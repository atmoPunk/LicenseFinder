import java.io.File
import org.apache.tika.detect.TextDetector

enum class License {
    MIT, APACHE2, GPL3, LGPL3, BSD3CLAUSE
}

class LicenseFinder {
    private val mitText = readStrippedFile("licenses/MIT.txt")
    private val apache2HeaderText = readStrippedFile("licenses/AP2_Header.txt")
    private val gpl3HeaderText = readStrippedFile("licenses/GPL3_Header.txt")
    private val bsd3ClText = readStrippedFile("licenses/BSD3Cl.txt")
    private val gpl3Text = readStrippedFile("licenses/GPL3.txt")
    private val lgpl3Text = readStrippedFile("licenses/LGPL3.txt")
    private val apache2Text = readStrippedFile("licenses/Apache2.txt")
    private val textDetector = TextDetector()

    fun getFileLicense(file: String): License? {
        return when {
            searchInFile(mitText, file) -> License.MIT
            searchInFile(apache2HeaderText, file) || searchInFile(apache2Text, file) -> License.APACHE2
            searchInFile(bsd3ClText, file) -> License.BSD3CLAUSE
            searchInFile(gpl3HeaderText, file) || searchInFile(gpl3Text, file) -> License.GPL3
            searchInFile(lgpl3Text, file) -> License.LGPL3
            else -> null
        }
    }

    fun getMainLicense(directory: String): License? {
        val licenseFile = getMainLicenseFile(directory) ?: return null
        return getFileLicense(licenseFile)

    }

    fun getLicenses(directory: String): Set<License> {
        val res = mutableSetOf<License>()
        for (textFile in getTextFiles(directory)) {
            res.add(getFileLicense(textFile) ?: continue)
        }
        return res
    }

    private fun getMainLicenseFile(directory: String): String? {
        for (file in File(directory).walkTopDown().maxDepth(1)) {
            when (file.nameWithoutExtension.toUpperCase()) {
                "LICENSE", "LICENCE", "COPYING" -> { return file.path }
                else -> continue
            }
        }
        return null
    }

    private fun getTextFiles(directory: String): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (file in File(directory).walkTopDown()) {
            if (file.isDirectory) {
                continue
            }
            val isText = textDetector.detect(file.inputStream().buffered(), null)
            if (isText.type == "text") { // We don't need to search binary files
                result.add(file.path)
            }
        }
        return result
    }

}

fun searchInFile(needle: String, haystackFile: String): Boolean {
    val haystack = readStrippedFile(haystackFile)
    return haystack.contains(needle)
}

fun readStrippedFile(file: String): String {
    // Reading file like this allows us to find licenses in comments
    // Or if some 'whitespace' characters were inserted or deleted in the license
    return File(file)
            .readText()
            .filter { it.isLetterOrDigit() }
            .map { it.toLowerCase() }
            .joinToString("")
}

fun main(args: Array<String>) {
    val licenseFinder = LicenseFinder()
    val licenses = licenseFinder.getLicenses(args[0])

    println("MIT: ${licenses.contains(License.MIT)}")
    println("Apache 2.0: ${licenses.contains(License.APACHE2)}")
    println("GPL 3.0: ${licenses.contains(License.GPL3)}")
    println("LGPL 3.0: ${licenses.contains(License.LGPL3)}")
    println("BSD 3-Clause: ${licenses.contains(License.BSD3CLAUSE)}")

    println("Main license: ${licenseFinder.getMainLicense(args[0])?.name}")
}