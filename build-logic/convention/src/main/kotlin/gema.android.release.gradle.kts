import com.android.build.api.dsl.ApkSigningConfig
import org.gradle.api.artifacts.VersionCatalog
import java.util.Properties

plugins {
    id("gema.android.application")
    id("com.android.application")
}

val catalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val keystoreProperties: Properties = Properties().apply {
    val declaredKeystore: File = rootProject.file("keystore.properties")
    if (declaredKeystore.exists()) {
        declaredKeystore.inputStream().use(::load)
    }
}

fun credential(property: String, environmentVariable: String): String? = keystoreProperties
    .getProperty(property)
    ?: providers.environmentVariable(environmentVariable).orNull

val storeFilePath: String? = credential("storeFile", "GEMA_STORE_FILE")
val storePasswordValue: String? = credential("storePassword", "GEMA_STORE_PASSWORD")
val keyAliasValue: String? = credential("keyAlias", "GEMA_KEY_ALIAS")
val keyPasswordValue: String? = credential("keyPassword", "GEMA_KEY_PASSWORD")

val uploadCredentials: List<String?> = listOf(storeFilePath, storePasswordValue, keyAliasValue, keyPasswordValue)
val hasUploadKey: Boolean = uploadCredentials.none(String?::isNullOrBlank)

android {
    defaultConfig {
        versionCode = catalog.findVersion("gemaVersionCode").get().requiredVersion.toInt()
        versionName = catalog.findVersion("gemaVersionName").get().requiredVersion
    }

    signingConfigs {
        if (hasUploadKey) {
            create("upload") {
                storeFile = rootProject.file(storeFilePath as String)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
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
