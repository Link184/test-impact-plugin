plugins {
    alias(libs.plugins.kotlin.dsl)
    id("com.vanniktech.maven.publish") version "0.30.0"
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(libs.versions.jdkVersion.get().toInt())
}

gradlePlugin {
    plugins {
        register("test-impact") {
            description = ""
            displayName = "Test impact"
            id = "com.link184.test-impact-plugin"
            implementationClass = "dev.sunnyday.test.impact.plugin.TestImpactPlugin"
        }
    }
}