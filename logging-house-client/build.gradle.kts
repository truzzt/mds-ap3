plugins {
    `java-library`
    `maven-publish`
    `jacoco`
}

val edcVersion: String by project
val edcGroup: String by project
val jupiterVersion: String by project
val mockitoVersion: String by project
val assertjVersion: String by project
val okHttpVersion: String by project
val jsonVersion: String by project

dependencies {
    implementation("${edcGroup}:control-plane-core:${edcVersion}")
    implementation("${edcGroup}:http-spi:${edcVersion}")
    implementation("${edcGroup}:sql-core:${edcVersion}")
    implementation("${edcGroup}:transaction-datasource-spi:${edcVersion}")

    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    implementation("org.json:json:${jsonVersion}")
    implementation("org.glassfish.jersey.media:jersey-media-multipart:3.1.3")

    implementation("de.fraunhofer.iais.eis.ids.infomodel:infomodel-java:1.0.2-basecamp")
    implementation("de.fraunhofer.iais.eis.ids.infomodel:infomodel-util:1.0.2-basecamp")

    implementation("org.postgresql:postgresql:42.4.5")
    implementation("org.flywaydb:flyway-core:9.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")

    testImplementation("${edcGroup}:core-spi:${edcVersion}")
    testImplementation("${edcGroup}:dsp-http-spi:${edcVersion}")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
