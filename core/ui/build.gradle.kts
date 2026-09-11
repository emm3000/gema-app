plugins {
    id("gema.android.compose")
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.material.icons.extended)
}
