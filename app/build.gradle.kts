import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.roborazzi)
}

android {
  namespace = "dev.arnv.bluke"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "dev.arnv.bluke"
    minSdk = 28
    targetSdk = 36
    // CI sets GITHUB_RUN_NUMBER, so every build has a higher versionCode and installs as an update
    versionCode = (System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0) + 100
    versionName = "1.1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val localProps = Properties()
      val localPropsFile = rootProject.file("local.properties")
      if (localPropsFile.exists()) {
        val stream = localPropsFile.inputStream()
        localProps.load(stream)
        stream.close()
      }

      val keystorePath = System.getenv("KEYSTORE_PATH")
          ?: localProps.getProperty("keystore.path")
          ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      
      storePassword = System.getenv("STORE_PASSWORD")
          ?: localProps.getProperty("keystore.password")
          
      keyAlias = System.getenv("KEY_ALIAS")
          ?: localProps.getProperty("keystore.alias")
          ?: "k0"
          
      keyPassword = System.getenv("KEY_PASSWORD")
          ?: localProps.getProperty("keystore.password")
          ?: System.getenv("STORE_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Own package name: never clashes with the original Bluke app
      applicationIdSuffix = ".fork"
      // Same signing key on every build: new APKs update over old ones
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    // Disables dependency metadata when building APKs.
    includeInApk = false
    // Disables dependency metadata when building Android App Bundles.
    includeInBundle = false
  }
}


dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.kotlinx.coroutines.android)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}



kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
  }
}
