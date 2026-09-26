import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "2.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Signing credentials are loaded from local.properties (gitignored, never uploaded)
    val keystoreProps = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

    signingConfigs {
        create("release") {
            val storePath = keystoreProps.getProperty("smartbook.storeFile")
            if (storePath != null) {
                storeFile = file(storePath)
                storePassword = keystoreProps.getProperty("smartbook.storePassword")
                keyAlias = keystoreProps.getProperty("smartbook.keyAlias")
                keyPassword = keystoreProps.getProperty("smartbook.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystoreProps.getProperty("smartbook.storeFile") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    implementation(libs.mpandroidchart)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // CardView (悬浮窗使用)
    implementation("androidx.cardview:cardview:1.0.0")

    // FlexboxLayout (分类网格使用)
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Glide (GIF 加载)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Java-only project — no Kotlin force-resolve needed
}


