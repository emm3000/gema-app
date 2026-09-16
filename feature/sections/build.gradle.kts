plugins {
    id("gema.android.feature")
}

dependencies {
    testImplementation(testFixtures(project(":core:ui")))
}
