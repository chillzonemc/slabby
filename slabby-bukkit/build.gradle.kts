plugins {
    id("io.github.goooler.shadow") version "8.1.7"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")

    // Plugin annotations for plugin.yml
    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")
    annotationProcessor("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")

    // Library for creating commands
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    // Library for interacting with server economy
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    // Library for handling configurations
    implementation("org.spongepowered:configurate-yaml:4.1.2")

    // Library for creating item UIs
    implementation("xyz.xenondevs.invui:invui:1.30")

    implementation(project(":slabby-api"))
    implementation(project(":slabby-sqlite3"))
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
    options.isFork = true;
}

//TODO: automatically relocate all shadowed dependencies
tasks.shadowJar {
    relocate("co.aikar.commands", "gg.mew.slabby.acf")
    relocate("co.aikar.locales", "gg.mew.slabby.locales")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}