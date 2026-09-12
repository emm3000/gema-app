package com.emm.gema.buildlogic

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ConventionPluginTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val fixture: ConventionPluginFixture
        get() = ConventionPluginFixture(temporaryFolder.root)

    @Test
    fun `android library plugin applies the shared android configuration`() {
        val report: Map<String, String> = fixture.report(listOf("gema.android.library"))

        assertThat(report["compileSdk"]).isEqualTo("37")
        assertThat(report["minSdk"]).isEqualTo("26")
        assertThat(report["sourceCompatibility"]).isEqualTo("17")
        assertThat(report["jvmTarget"]).isEqualTo("17")
        assertThat(report["namespace"]).isEqualTo("com.emm.gema.probe")
        assertThat(report["optIn"]).isEqualTo("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }

    @Test
    fun `android compose plugin turns compose on over the library configuration`() {
        val report: Map<String, String> = fixture.report(listOf("gema.android.compose"))

        assertThat(report["compileSdk"]).isEqualTo("37")
        assertThat(report["minSdk"]).isEqualTo("26")
        assertThat(report["jvmTarget"]).isEqualTo("17")
        assertThat(report["compose"]).isEqualTo("true")
        assertThat(report["optIn"]).contains("androidx.compose.material3.ExperimentalMaterial3Api")
        assertThat(report["optIn"]).contains("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }

    @Test
    fun `android feature plugin adds the shared feature dependencies`() {
        val report: Map<String, String> = fixture.report(listOf("gema.android.feature"))

        assertThat(report["compose"]).isEqualTo("true")
        assertThat(report["projectDependencies"]).isEqualTo(":core:domain,:core:ui")
    }

    @Test
    fun `android application plugin applies the shared android configuration`() {
        val report: Map<String, String> = fixture.report(
            pluginIds = listOf("gema.android.application"),
            androidConfiguration = APPLICATION_CONFIGURATION
        )

        assertThat(report["compileSdk"]).isEqualTo("37")
        assertThat(report["minSdk"]).isEqualTo("26")
        assertThat(report["targetSdk"]).isEqualTo("37")
        assertThat(report["sourceCompatibility"]).isEqualTo("17")
        assertThat(report["jvmTarget"]).isEqualTo("17")
        assertThat(report["compose"]).isEqualTo("true")
    }

    @Test
    fun `android release plugin reads the version from the catalog and minifies the release build`() {
        val report: Map<String, String> = fixture.report(
            pluginIds = listOf("gema.android.release"),
            androidConfiguration = APPLICATION_CONFIGURATION
        )

        assertThat(report["versionCode"]).isEqualTo("1")
        assertThat(report["versionName"]).isEqualTo("1.0")
        assertThat(report["minifyRelease"]).isEqualTo("true")
        assertThat(report["compileSdk"]).isEqualTo("37")
    }

    @Test
    fun `jvm library plugin targets java 17 and mirrors the android test task name`() {
        val report: Map<String, String> = fixture.report(listOf("gema.jvm.library"))

        assertThat(report["javaSourceCompatibility"]).isEqualTo("17")
        assertThat(report["jvmTarget"]).isEqualTo("17")
        assertThat(report["conventionTasks"]).isEqualTo("testDebugUnitTest")
        assertThat(report["optIn"]).isEqualTo("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }

    @Test
    fun `sqldelight plugin registers the snapshot check`() {
        val report: Map<String, String> = fixture.report(listOf("gema.android.library", "gema.sqldelight"))

        assertThat(report["conventionTasks"]).contains("checkSqlDelightSnapshots")
        assertThat(report["compileSdk"]).isEqualTo("37")
    }

    private companion object {

        val APPLICATION_CONFIGURATION: String = """
            android {
                namespace = "com.emm.gema.probe"

                defaultConfig {
                    applicationId = "com.emm.gema.probe"
                }
            }
        """.trimIndent()
    }
}
