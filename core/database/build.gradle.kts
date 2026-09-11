plugins {
    id("gema.android.library")
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("GemaDb") {
            packageName.set("com.emm.gema.core.database")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.coroutines.extensions)
    testImplementation(libs.sqldelight.sqlite.driver)
}
