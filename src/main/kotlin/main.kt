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

    val licenseFinder = LicenseFinder()
    val licenses = licenseFinder.getLicenses(directory)


    println("Licenses found: ")
    for (license in licenses) {
        println(license.name)
    }

    println("Main license: ${licenseFinder.getMainLicense(directory)?.name}")
}
