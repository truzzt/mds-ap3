plugins {
    id("java")
    id("checkstyle")
    id("maven-publish")
    id("org.sonarqube") version "4.4.1.3373"
}

val jupiterVersion: String by project
val mockitoVersion: String by project
val sonarProjectKey: String by project
val sonarOrganization: String by project
val sonarHostUrl: String by project

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
}

val downloadArtifact: Configuration by configurations.creating {
    isTransitive = false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "org.sonarqube")

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    checkstyle {
        toolVersion = "10.9.3"
        configFile = rootProject.file("checkstyle/checkstyle-config.xml")
        configDirectory.set(rootProject.file("checkstyle"))
        maxErrors = 0 // does not tolerate errors
    }

    sonar {
        properties {
            property("sonar.projectKey", sonarProjectKey)
            property("sonar.organization", sonarOrganization)
            property("sonar.host.url", sonarHostUrl)
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/ids-basecamp/ids-infomodel-java")
                credentials {
                    username = System.getenv("USERNAME")
                    password = System.getenv("TOKEN")
                }
            }
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/truzzt/mds-ap3")
                credentials {
                    username = System.getenv("USERNAME")
                    password = System.getenv("TOKEN")
                }
            }
        }
    }
}
