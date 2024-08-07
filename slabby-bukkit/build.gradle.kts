import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import gg.mew.slabby.SlabbyDependencies

plugins {
    id("io.github.goooler.shadow") version "8.1.8"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:${SlabbyDependencies.PAPER_API}")

    // Plugin annotations for plugin.yml
    compileOnly("org.spigotmc:plugin-annotations:${SlabbyDependencies.PLUGIN_ANNOTATIONS}")
    annotationProcessor("org.spigotmc:plugin-annotations:${SlabbyDependencies.PLUGIN_ANNOTATIONS}")

    // Library for creating commands
    implementation("co.aikar:acf-paper:${SlabbyDependencies.ACF}")

    // Library for interacting with server economy
    compileOnly("com.github.MilkBowl:VaultAPI:${SlabbyDependencies.VAULT}")

    // Library for handling yaml
    implementation("org.spongepowered:configurate-yaml:${SlabbyDependencies.CONFIGURATE}")

    // Library for creating item UIs
    implementation("xyz.xenondevs.invui:invui:${SlabbyDependencies.INVUI}")

    // Library for integrating with the Lands claims plugin
    compileOnly("com.github.angeschossen:LandsAPI:${SlabbyDependencies.LANDS}")

    // Library for handling json
    implementation("com.google.code.gson:gson:${SlabbyDependencies.GSON}")

    implementation(project(":slabby-api"))
    implementation(project(":slabby-sqlite3"))
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
    options.isFork = true
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}