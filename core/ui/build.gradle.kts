plugins {
    id("gema.android.compose")
    id("gema.android.library.testfixtures")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.material.icons.extended)

    testFixturesApi(platform(libs.androidx.compose.bom))
    testFixturesApi(libs.androidx.compose.ui.test.junit4)
    testFixturesApi(libs.robolectric)
    testFixturesApi(libs.junit)
}
