plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {

    namespace = "com.aistudio.gestordejoias.jrwlpq"

    compileSdk = 35

    defaultConfig {

        applicationId =
            "com.aistudio.gestordejoias.jrwlpq"

        minSdk = 24
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {

        release {

            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }

    kotlinOptions {

        jvmTarget = "11"
    }

    buildFeatures {

        compose = true
    }

    composeOptions {

        kotlinCompilerExtensionVersion =
            "1.5.15"
    }
}

dependencies {

    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(libs.androidx.core.ktx)

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.compose.material3
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}
