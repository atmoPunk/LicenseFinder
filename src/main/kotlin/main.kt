import java.io.File
import kotlin.system.exitProcess

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

    if (!directory.canRead()) {
        System.err.println("Can't read ${args[0]}: permission denied")
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
