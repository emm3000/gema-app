plugins {
    id("gema.android.library")
    id("gema.sqldelight")
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.coroutines.extensions)
    testImplementation(libs.sqldelight.sqlite.driver)
}
