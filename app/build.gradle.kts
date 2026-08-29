plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.secrets)
    id("com.google.devtools.ksp")
}

val byteBuddyAgent by configurations.creating

android {
    namespace = "com.siraj.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.siraj.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "FIREBASE_API_KEY", "\"dummy\"")
        buildConfigField("String", "GOOGLE_PLAY_PACKAGE_NAME", "\"dummy\"")
        buildConfigField("String", "GOOGLE_PLAY_PUBSUB_TOPIC_NAME", "\"dummy\"")
        buildConfigField("String", "GOOGLE_PLAY_SERVICE_ACCOUNT_JSON", "\"dummy\"")
        buildConfigField("String", "APP_STORE_BUNDLE_ID", "\"dummy\"")
        buildConfigField("String", "APP_STORE_ENVIRONMENT", "\"dummy\"")
        buildConfigField("String", "APP_STORE_ISSUER_ID", "\"dummy\"")
        buildConfigField("String", "APP_STORE_KEY_ID", "\"dummy\"")
        buildConfigField("String", "APP_STORE_PRIVATE_KEY", "\"dummy\"")
        buildConfigField("String", "ENVIRONMENT", "\"development\"")
        buildConfigField("Boolean", "IS_BETA", "true")
        buildConfigField("Boolean", "ALLOW_MOCK_DATA", "true")
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { testTask ->
                val agentJar = byteBuddyAgent.asPath
                testTask.jvmArgs(
                    "-javaagent:$agentJar",
                    "-XX:+EnableDynamicAgentLoading",
                    "-Xshare:off"
                )
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // ExoPlayer for Audio/Video
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")
    
    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    
    // Markdown
    
    // Compose Charts (e.g. for studio analytics)

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.13")
    byteBuddyAgent("net.bytebuddy:byte-buddy-agent:1.14.17")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core-ktx:1.5.0")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-functions-ktx")
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
}

secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
}
