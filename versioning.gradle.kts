import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

val versionProps = Properties()
val versionPropertiesFile = rootProject.file("version.properties")
if (versionPropertiesFile.exists()) {
    versionPropertiesFile.inputStream().use { versionProps.load(it) }
} else {
    throw GradleException("Root project version.properties not found! Please ensure it exists with MAJOR, MINOR, PATCH values.")
}
fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("yyyyMMddHHmm")
    return sdf.format(Date())
}

// this is for android app. desktop app not need
fun appVersionCode(): Int {
    val ciBuildNumber = System.getenv("GITHUB_RUN_NUMBER")
    return if (ciBuildNumber != null) {
        ciBuildNumber.toInt() + 1
    } else {
        1 // Local build version code
    }
}

val appMajorVersion = versionProps.getProperty("MAJOR").toInt()
val appMinorVersion = versionProps.getProperty("MINOR").toInt()
val appPatchVersion = versionProps.getProperty("PATCH").toInt()

fun appVersionName(): String {
    return "${appMajorVersion}.${appMinorVersion}.${appPatchVersion}"
}

project.extra.set("appVersionCode", ::appVersionCode)
project.extra.set("appVersionName", ::appVersionName)
