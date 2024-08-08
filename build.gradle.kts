plugins {
    id("java-library")
}

subprojects {
    apply(plugin = "java-library")

    group = "gg.mew.slabby"
    version = "1.0"

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")

        testCompileOnly("org.projectlombok:lombok:1.18.34")
        testAnnotationProcessor("org.projectlombok:1.18.34")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}