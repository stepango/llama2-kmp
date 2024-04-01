plugins {
    kotlin("jvm")
    application
}

group = "com.stepango.llama2-kmp"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("Main")
}

dependencies {
    implementation("com.squareup.okio:okio:3.9.0")
    implementation(project(mapOf("path" to ":llama2")))
    project(":llama2")
}
