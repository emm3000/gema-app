plugins {
    id("gema.android.release")
}

android {
    namespace = "com.emm.gema"

    defaultConfig {
        applicationId = "com.emm.gema"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:siagie"))
    implementation(project(":core:ui"))
    implementation(project(":feature:setup"))
    implementation(project(":feature:sections"))
    implementation(project(":feature:students"))
    implementation(project(":feature:attendance"))
    implementation(project(":feature:evaluation"))
    implementation(project(":feature:activities"))
    implementation(project(":feature:export"))
    implementation(project(":feature:backup"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.compose)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.truth)

    testImplementation(testFixtures(project(":core:ui")))
}
