# Clients


## Java EDC Extension

### Integrating with Maven Repository

To use the Maven package from our repository, update your `build.gradle.kts` with the following repository configuration:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/truzzt/mds-ap3")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}
```

This configuration includes the necessary Maven repository for our package and sets up authentication using either project properties or environment variables.

### Setting Up Authentication

To access our public Maven repository, authentication is required. Please follow the detailed [GitHub Packages with Apache Maven documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages) for guidance on setting up authentication.

### Adding the Dependency

Once the repository is added and authentication is configured, you can include the package in your dependencies:

```kotlin
dependencies {
    implementation("logging-house:logging-house-client:0.1.0")
}
```

### Environment Configuration

The `logging-house-client` relies on two key environment variables for configuration:

| Name                                  | Default | Description                                                  |
| ------------------------------------- | ------- | ------------------------------------------------------------ |
| `CLEARINGHOUSE_CLIENT_EXTENSION_ENABLED` | `false` | Set to `true` to enable the extension, or `false` to disable it. |
| `EDC_CLEARINGHOUSE_LOG_URL`           | `none`  | Specify the URL of the Logging-House-Server (e.g., `clearing.demo.truzzt.eu`). |

Ensure these environment variables are set as per your requirements for optimal functionality of the client.
