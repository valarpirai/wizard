plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:11.0.0")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.0")
    implementation("org.eclipse.jetty:jetty-webapp:11.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

}

tasks.test {
    useJUnitPlatform()
}
