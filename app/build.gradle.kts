plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    pmd
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

val fdcApiKey = providers.gradleProperty("FDC_API_KEY").orNull ?: "jm1CkmTFOs3ywJpl3sCM3O6vlev4yg1IigaaggJ1"

android {
    namespace = "com.example.cst438project1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cst438project1"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "FDC_API_KEY",
            "\"$fdcApiKey\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.kotlinx.coroutines.android)

    // Adding to the pmd configuration drops Gradle's defaults, so list all three
    pmd("net.sourceforge.pmd:pmd-ant:7.27.0")
    pmd("net.sourceforge.pmd:pmd-java:7.27.0")
    pmd("net.sourceforge.pmd:pmd-kotlin:7.27.0")
}

pmd {
    toolVersion = "7.27.0"
    isConsoleOutput = true
    isIgnoreFailures = false
    ruleSetFiles = files("$rootDir/config/pmd/ruleset.xml")
    ruleSets = listOf()
}

// Android has no Java sourceSets, so the pmd plugin creates no tasks on its own
val pmdKotlin by tasks.registering(Pmd::class) {
    description = "Runs PMD on Kotlin sources."
    group = "verification"
    source = fileTree("src") { include("**/*.kt") }
    classpath = files()
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.named("check") { dependsOn(pmdKotlin) }

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    // Pre-existing long composables and sample data; new code is still checked
    baseline = file("$rootDir/config/detekt/baseline.xml")
    source.setFrom("src/main/java", "src/test/java", "src/androidTest/java")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}