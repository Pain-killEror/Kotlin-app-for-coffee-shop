plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id ("kotlin-parcelize")
}

android {
    namespace = "com.example.itrysohard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.itrysohard"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }


}

dependencies {

    implementation ("com.google.crypto.tink:tink-android:1.7.0")


    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation ("com.intuit.sdp:sdp-android:1.1.0")
    implementation ("com.intuit.ssp:ssp-android:1.1.0")

    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
    implementation ("androidx.appcompat:appcompat:1.5.1'")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation ("org.mindrot:jbcrypt:0.4")
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.places)
    //implementation(libs.androidx.security.crypto.ktx)
    // Room Database

    kapt("androidx.room:room-compiler:2.6.0")


    // Зависимость для Room
    implementation ("androidx.room:room-runtime:2.5.0") // используйте вашу версию Room
    annotationProcessor ("androidx.room:room-compiler:2.5.0") // или kapt для Kotlin
    implementation ("androidx.room:room-ktx:2.5.0") // добавьте эту зависимость
    //implementation ("androidx.security:security-crypto-ktx:2.5.0") // добавьте эту зависимость

    // Зависимость для Kotlin coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1") // используйте актуальную версию


    implementation ("com.google.code.gson:gson:2.8.8")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0") // Современный вариант для жизненного цикла
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0") // Использование ViewModel

    // Image Loading
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    implementation("androidx.exifinterface:exifinterface:1.3.3")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okio:okio:3.5.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.0")

    // Core and UI
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0") // Версия может измениться
    implementation("androidx.core:core-ktx:1.16.0") // Для использования расширений Kotlin

    // Unit and UI Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
