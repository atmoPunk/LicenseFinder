import java.io.File
import org.apache.tika.detect.TextDetector
import org.apache.tika.io.IOUtils
import kotlin.system.exitProcess

enum class License {
    MIT, APACHE2, GPL3, LGPL3, BSD3CLAUSE, UNKNOWN
}

interface LicenseFinder {
    fun getFileLicense(file: File): License?
    fun getLicenses(directory: File): Set<License>
    fun getMainLicense(directory: File): License?
}

fun String.getLettersAndDigits(): String {
    return this
            .filter { it.isLetterOrDigit() }
            .map { it.toLowerCase() }
            .joinToString("")
}

class SimpleLicenseFinder : LicenseFinder {
    override fun getFileLicense(file: File): License? {
        return when {
            searchInFile(mitText, file) -> License.MIT
            searchInFile(apache2HeaderText, file) || searchInFile(apache2Text, file) -> License.APACHE2
            searchInFile(bsd3ClText, file) -> License.BSD3CLAUSE
            searchInFile(gpl3HeaderText, file) || searchInFile(gpl3Text, file) -> License.GPL3
            searchInFile(lgpl3Text, file) -> License.LGPL3
            else -> null
        }
    }

    override fun getMainLicense(directory: File): License? {
        val licenseFile = getMainLicenseFile(directory) ?: return null
        return getFileLicense(licenseFile) ?: License.UNKNOWN
        // return UNKNOWN if LICENSE.txt or similar is present, but we don't know the actual license
    }

    override fun getLicenses(directory: File): Set<License> {
        val res = mutableSetOf<License>()
        for (textFile in getTextFiles(directory)) {
            res.add(getFileLicense(textFile) ?: continue)
        }
        return res
    }

    private fun getMainLicenseFile(directory: File): File? {
        for (file in directory.walkTopDown().maxDepth(1)) {
            when (file.nameWithoutExtension.toUpperCase()) {
                "LICENSE", "LICENCE", "COPYING" -> { return file }
                else -> continue
            }
        }
        return null
    }

    private fun getTextFiles(directory: File): ArrayList<File> {
        val result: ArrayList<File> = ArrayList()
        for (file in directory.walkTopDown()) {
            if (file.isDirectory) {
                continue
            }
            val isText = file.inputStream().buffered().use {
                textDetector.detect(it, null)
            }
            if (isText.type == "text") { // We don't need to search binary files
                result.add(file)
            }
        }
        return result
    }

    private companion object {
        private fun readStrippedResourceFile(resource: String): String {
            // Reading file like this allows us to find licenses in comments
            // Or if some 'whitespace' characters were inserted or deleted in the license
            return SimpleLicenseFinder::class.java.getResourceAsStream(resource).use {
                IOUtils.toString(it).getLettersAndDigits()
            }
        }

        private val mitText = readStrippedResourceFile("/licenses/MIT.txt")
        private val apache2HeaderText = readStrippedResourceFile("/licenses/AP2_Header.txt")
        private val gpl3HeaderText = readStrippedResourceFile("/licenses/GPL3_Header.txt")
        private val bsd3ClText = readStrippedResourceFile("/licenses/BSD3Cl.txt")
        private val gpl3Text = readStrippedResourceFile("/licenses/GPL3.txt")
        private val lgpl3Text = readStrippedResourceFile("/licenses/LGPL3.txt")
        private val apache2Text = readStrippedResourceFile("/licenses/Apache2.txt")
        private val textDetector = TextDetector()
    }
}

fun searchInFile(needle: String, haystackFile: File): Boolean {
    val haystack = haystackFile.readText().getLettersAndDigits()
    return haystack.contains(needle)
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Wrong number of arguments, got ${args.size}, expected 1")
        System.err.println("Usage: java -jar FindLicenses.jar directory")
        exitProcess(1)
    }

    val directory = File(args[0])

    if (!directory.exists()) {
        System.err.println("Directory ${args[0]} does not exist")
        exitProcess(1)
    }
    if (!directory.isDirectory) {
        System.err.println("File ${args[0]} is not a directory")
        exitProcess(1)
    }

    val licenseFinder = SimpleLicenseFinder()
    val licenses = licenseFinder.getLicenses(directory)

    println("MIT: ${licenses.contains(License.MIT)}")
    println("Apache 2.0: ${licenses.contains(License.APACHE2)}")
    println("GPL 3.0: ${licenses.contains(License.GPL3)}")
    println("LGPL 3.0: ${licenses.contains(License.LGPL3)}")
    println("BSD 3-Clause: ${licenses.contains(License.BSD3CLAUSE)}")

    println("Main license: ${licenseFinder.getMainLicense(directory)?.name}")
}
