import org.apache.tika.detect.TextDetector
import org.apache.tika.io.IOUtils
import java.io.File

enum class License {
    MIT, APACHE2, GPL3, LGPL3, BSD3CLAUSE, UNKNOWN
}

fun String.getLettersAndDigits(): String {
    return this
            .asSequence()
            .filter { it.isLetterOrDigit() }
            .map { it.toLowerCase() }
            .joinToString("")
}

class LicenseFinder {
     fun getFileLicense(file: File): License? {
        return when {
            searchInFile(mitText, file) -> License.MIT
            searchInFile(apache2HeaderText, file) || searchInFile(apache2Text, file) -> License.APACHE2
            searchInFile(bsd3ClText, file) -> License.BSD3CLAUSE
            searchInFile(gpl3HeaderText, file) || searchInFile(gpl3Text, file) -> License.GPL3
            searchInFile(lgpl3Text, file) -> License.LGPL3
            else -> null
        }
    }

    fun getMainLicense(directory: File): License? {
        val licenseFile = getMainLicenseFile(directory) ?: return null
        return getFileLicense(licenseFile) ?: License.UNKNOWN
        // returns UNKNOWN if LICENSE.txt or similar is present, but we don't know the actual license
    }

    fun getLicenses(directory: File): Set<License> {
        return getTextFiles(directory)
                .mapNotNull { getFileLicense(it) }
                .toSet()
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

    private fun getTextFiles(directory: File): List<File> {
        val result = mutableListOf<File>()
        for (file in directory.walkTopDown()) {
            if (file.isDirectory) {
                continue
            }
            if (!file.canRead()) {
                System.err.println("Skipping ${file}: permission denied")
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
            return LicenseFinder::class.java.getResourceAsStream(resource).use {
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