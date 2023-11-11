import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.worldsticher360"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.worldsticher360"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(files("D:\\SFU COURSES\\FALL 2023\\IAT 359\\Work\\Final Project\\app\\libs\\javacpp.jar"))
    implementation(files("D:\\SFU COURSES\\FALL 2023\\IAT 359\\Work\\Final Project\\app\\libs\\javacv.jar"))
    implementation(files("D:\\SFU COURSES\\FALL 2023\\IAT 359\\Work\\Final Project\\app\\libs\\opencv.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//    implementation ("org.bytedeco:javacv-platform:1.5.7")
}