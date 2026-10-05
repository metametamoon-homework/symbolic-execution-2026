plugins {
  // Apply the shared build logic from a convention plugin.
  // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
  id("buildsrc.convention.kotlin-jvm")

  // Apply the Application plugin to add support for building an executable JVM application.
  application
  alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
  implementation(project(":utils"))

  implementation(libs.bundles.kotlinxEcosystem)
  implementation(files("/home/metametamoon/z3-5.1.0-x64-glibc-2.39/bin/com.microsoft.z3.jar"))
}

application {
  // Define the Fully Qualified Name for the application main class
  // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
  mainClass = "com.github.metametamoon.app.AppKt"
}
