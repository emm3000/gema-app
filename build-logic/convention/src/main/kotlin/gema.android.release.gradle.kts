import com.android.build.api.dsl.ApkSigningConfig
import org.gradle.api.artifacts.VersionCatalog
import java.util.Properties

plugins {
    id("gema.android.application")
}

val catalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val declaredKeystore: RegularFile = rootProject.layout.projectDirectory.file("keystore.properties")
val declaredCredentials: String = providers.fileContents(declaredKeystore).asText.orNull.orEmpty()

val keystoreProperties: Properties = Properties().apply {
    if (declaredCredentials.isNotBlank()) {
        declaredCredentials.reader().use(::load)
    }
}

fun credential(property: String, environmentVariable: String): String? = keystoreProperties
    .getProperty(property)
    ?: providers.environmentVariable(environmentVariable).orNull

val uploadCredentials: Map<String, String?> = mapOf(
    "storeFile" to credential("storeFile", "GEMA_STORE_FILE"),
    "storePassword" to credential("storePassword", "GEMA_STORE_PASSWORD"),
    "keyAlias" to credential("keyAlias", "GEMA_KEY_ALIAS"),
    "keyPassword" to credential("keyPassword", "GEMA_KEY_PASSWORD")
)

val missingCredentials: List<String> = uploadCredentials.filterValues(String?::isNullOrBlank).keys.toList()
val hasUploadKey: Boolean = missingCredentials.isEmpty()
val hasPartialUploadKey: Boolean = !hasUploadKey && missingCredentials.size < uploadCredentials.size

if (hasPartialUploadKey) {
    logger.warn(
        "Release signing is off: {} missing from keystore.properties and from the environment. " +
            "The release build will be unsigned.",
        missingCredentials.joinToString()
    )
}

android {
    defaultConfig {
        versionCode = catalog.findVersion("gemaVersionCode").get().requiredVersion.toInt()
        versionName = catalog.findVersion("gemaVersionName").get().requiredVersion
    }

    signingConfigs {
        if (hasUploadKey) {
            create("upload") {
                storeFile = rootProject.file(uploadCredentials.getValue("storeFile") as String)
                storePassword = uploadCredentials.getValue("storePassword")
                keyAlias = uploadCredentials.getValue("keyAlias")
                keyPassword = uploadCredentials.getValue("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (hasUploadKey) signingConfigs.getByName("upload") as ApkSigningConfig else null
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
