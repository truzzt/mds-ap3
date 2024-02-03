plugins {
    id("java")
    id("checkstyle")
    id("maven-publish")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.mockito:mockito-core:3.6.28")
}

val downloadArtifact: Configuration by configurations.creating {
    isTransitive = false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")

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
                version = "0.2.0"
                credentials {
                    username = System.getenv("USERNAME")
                    password = System.getenv("TOKEN")
                }
            }
        }
    }
}
