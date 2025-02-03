plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.fideicomisoapproverring"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fideicomisoapproverring"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "LOBSTR_SIGNATURE_HASH", properties["LOBSTR_SIGNATURE_HASH"].toString())
        buildConfigField("String", "APP_SECRET_KEY", properties["APP_SECRET_KEY"].toString())
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        getByName("debug") {
            isDebuggable = true
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
            excludes.add("META-INF/MANIFEST.MF")
            excludes.add("META-INF/*.RSA")
            excludes.add("META-INF/*.SF")
            excludes.add("META-INF/*.DSA")
        }
    }

    configurations.all {
        exclude(module = "conceal")
        exclude(module = "bcprov-jdk15on")
    }

    androidResources {
        generateLocaleConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                it.testLogging {
                    events("passed", "skipped", "failed")
                    it.outputs.upToDateWhen { false }
                }
            }
        }
    }
}

dependencies {
    implementation(projects.onboarding.dashboard)
    implementation(projects.theme)
    implementation(libs.androidx.multidex)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.squareup.okhttp)
    implementation(libs.wallet.sdk)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.squareup.okhttp)
    implementation(libs.json)

    implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    // Navigation
    implementation(libs.androidx.ui.compose.navigation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)

    implementation(libs.lottie)
    implementation(libs.androidx.encryption)
    implementation(libs.identity.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.github.ringofringssdk)

    implementation(libs.java.stellar.sdk) {
        exclude(group = "org.bouncycastle")
    }

    implementation(libs.bouncy.castle)

    // Testing Dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.core.ktx)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.junit.ktx)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.squareup.okhttp)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.appcompat)



    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.androidx.espresso.intents)
}

subprojects {
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.bouncycastle") {
                    useVersion("1.68")
                    because("Avoiding duplicate classes from different versions")
                }
            }
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("org.bouncycastle:bcprov-jdk18on:1.78.1")
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
        exclude(group = "org.bouncycastle", module="bcprov-jdk15on")
    }
}
