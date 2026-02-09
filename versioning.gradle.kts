import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

val versionProps = Properties()
val versionPropertiesFile = rootProject.file("composeApp/src/jvmMain/resources/props.properties")
if (versionPropertiesFile.exists()) {
    versionPropertiesFile.inputStream().use { versionProps.load(it) }
} else {
    throw GradleException("Root project version.properties not found! Please ensure it exists with the version number.")
}
fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("yyyyMMddHHmm")
    return sdf.format(Date())
}

fun appVersionCode(): Int {
    val ciBuildNumber = System.getenv("GITHUB_RUN_NUMBER")
    return if (ciBuildNumber != null) {
        ciBuildNumber.toInt() + 1
    } else {
        1 // Local build version code
    }
}

fun appVersionName(): String {
    return versionProps.getProperty("version")
}

project.extra.set("appVersionCode", ::appVersionCode)
project.extra.set("appVersionName", ::appVersionName)
