plugins {
    `java-library`
    `maven-publish`
}

val edcVersion: String by project
val edcGroup: String by project
val jupiterVersion: String by project
val mockitoVersion: String by project
val assertj: String by project
val okHttpVersion: String by project
val jsonVersion: String by project

dependencies {
    implementation("${edcGroup}:control-plane-core:${edcVersion}")
    implementation("${edcGroup}:http-spi:${edcVersion}")

    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    implementation("org.json:json:${jsonVersion}")
    implementation("org.glassfish.jersey.media:jersey-media-multipart:3.1.3")

    implementation("de.fraunhofer.iais.eis.ids.infomodel:infomodel-java:1.0.2-basecamp")
    implementation("de.fraunhofer.iais.eis.ids.infomodel:infomodel-util:1.0.2-basecamp")

    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
