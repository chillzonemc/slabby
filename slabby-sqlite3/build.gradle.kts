dependencies {
    compileOnly(project(":slabby-api"))

    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")
}
