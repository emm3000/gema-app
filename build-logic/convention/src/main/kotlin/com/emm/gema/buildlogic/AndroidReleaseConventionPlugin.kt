package com.emm.gema.buildlogic

import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.api.dsl.ApplicationBuildType
import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.kotlin.dsl.getByType

class AndroidReleaseConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("gema.android.application")

            val declaredKeystore: RegularFile = rootProject.layout.projectDirectory.file("keystore.properties")
            val declaredCredentials: String = providers.fileContents(declaredKeystore).asText.orNull.orEmpty()

            val keystoreProperties: Properties = Properties().apply {
                if (declaredCredentials.isNotBlank()) {
                    declaredCredentials.reader().use(::load)
                }
            }

            val uploadCredentials: Map<String, String?> = mapOf(
                "storeFile" to credential(keystoreProperties, "storeFile", "GEMA_STORE_FILE"),
                "storePassword" to credential(keystoreProperties, "storePassword", "GEMA_STORE_PASSWORD"),
                "keyAlias" to credential(keystoreProperties, "keyAlias", "GEMA_KEY_ALIAS"),
                "keyPassword" to credential(keystoreProperties, "keyPassword", "GEMA_KEY_PASSWORD")
            )

            val missingCredentials: List<String> = uploadCredentials
                .filterValues(String?::isNullOrBlank)
                .keys
                .toList()
            val hasUploadKey: Boolean = missingCredentials.isEmpty()
            val hasPartialUploadKey: Boolean = !hasUploadKey && missingCredentials.size < uploadCredentials.size

            if (hasPartialUploadKey) {
                logger.warn(
                    "Release signing is off: {} missing from keystore.properties and from the environment. " +
                        "The release build will be unsigned.",
                    missingCredentials.joinToString()
                )
            }

            val extension: ApplicationExtension = extensions.getByType<ApplicationExtension>()

            extension.defaultConfig.versionCode = libs.version("gemaVersionCode").toInt()
            extension.defaultConfig.versionName = libs.version("gemaVersionName")

            val uploadSigningConfig: ApkSigningConfig? = when {
                hasUploadKey -> extension.signingConfigs.create("upload").apply {
                    storeFile = rootProject.file(uploadCredentials.getValue("storeFile") as String)
                    storePassword = uploadCredentials.getValue("storePassword")
                    keyAlias = uploadCredentials.getValue("keyAlias")
                    keyPassword = uploadCredentials.getValue("keyPassword")
                }
                else -> null
            }

            val releaseBuildType: ApplicationBuildType = extension.buildTypes.getByName("release")
            releaseBuildType.isMinifyEnabled = true
            releaseBuildType.isShrinkResources = true
            releaseBuildType.signingConfig = uploadSigningConfig
            releaseBuildType.proguardFiles(
                extension.getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    private fun Project.credential(
        keystoreProperties: Properties,
        property: String,
        environmentVariable: String
    ): String? = keystoreProperties.getProperty(property)
        ?: providers.environmentVariable(environmentVariable).orNull
}
