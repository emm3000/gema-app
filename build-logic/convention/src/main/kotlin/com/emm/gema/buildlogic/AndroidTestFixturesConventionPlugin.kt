package com.emm.gema.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidTestFixturesConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            extensions.getByType<LibraryExtension>().testFixtures.enable = true
        }
    }
}
