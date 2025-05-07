plugins {
    id("application")
    kotlin("jvm") version "2.1.10"
}

group = "agh.io"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-netty:1.71.0")
    implementation("io.grpc:grpc-protobuf:1.71.0")
    implementation("io.grpc:grpc-services:1.71.0")
    implementation("io.grpc:grpc-stub:1.71.0")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("com.google.protobuf:protobuf-kotlin:4.30.2")
    implementation("com.google.protobuf:protobuf-java:4.30.2")
}

application {
    mainClass = "MainKt"
}

kotlin {
    jvmToolchain(21)
}

sourceSets["main"].java.srcDirs("src/main/gen", "src/main/kotlin")