buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
    repositories {
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.sqldelight).apply(false)
    alias(libs.plugins.google.services).apply(false)
    id("com.google.devtools.ksp") version "2.0.0-1.0.23"
}
