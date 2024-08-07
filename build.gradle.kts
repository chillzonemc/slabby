import gg.mew.slabby.SlabbyDependencies
import gg.mew.slabby.SlabbyVersion

plugins {
    id("java-library")
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "java-library")

    group = SlabbyVersion.PACKAGE
    version = SlabbyVersion.RELEASE

    dependencies {
        compileOnly("org.projectlombok:lombok:${SlabbyDependencies.LOMBOK}")
        annotationProcessor("org.projectlombok:lombok:${SlabbyDependencies.LOMBOK}")

        testCompileOnly("org.projectlombok:lombok:${SlabbyDependencies.LOMBOK}")
        testAnnotationProcessor("org.projectlombok:${SlabbyDependencies.LOMBOK}")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}