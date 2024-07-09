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
    implementation("logging-house:logging-house-client:v1.1.0")
}
```

### Environment Configuration

The `logging-house-client` relies on two key environment variables for configuration:

| Name                                  | Default | Description                                                  |
| ------------------------------------- | ------- | ------------------------------------------------------------ |
| `EDC_LOGGINGHOUSE_EXTENSION_ENABLED` | `false` | Set to `true` to enable the extension, or `false` to disable it. |
| `EDC_LOGGINGHOUSE_LOG_URL`           | `none`  | Specify the URL of the Logging-House-Server (e.g., `clearing.demo.truzzt.eu`). |
| `EDC_DATASOURCE_LOGGINGHOUSE_URL` | `none` | Specify the URL of the database (e.g., `postgres://some-url`). |
| `EDC_DATASOURCE_LOGGINGHOUSE_USER` | `none` | Specify the user of the database. |
| `EDC_DATASOURCE_LOGGINGHOUSE_PASSWORD` | `none` | Specify the password of the database. |

Ensure these environment variables are set as per your requirements for optimal functionality of the client.

### Advanced Environment Configuration
| Name                                  | Default | Description                                                  |
| ------------------------------------- | ------- | ------------------------------------------------------------ |
| `EDC_LOGGINGHOUSE_EXTENSION_FLYWAY_REPAIR` | `false` | Recreates the flyway history tables, beafore applying the scripts. |
| `EDC_LOGGINGHOUSE_EXTENSION_FLYWAY_CLEAN` | `false`  | Executes a clean on the database, before applying the scripts. |
| `EDC_LOGGINGHOUSE_EXTENSION_WORKERS_MAX` | `1` | Maximun number of workers created to handle the pending items. |
| `EDC_LOGGINGHOUSE_EXTENSION_WORKERS_DELTA` | `30` | Time in seconds, after the connector initializes, to the workers start to run. |
| `EDC_LOGGINGHOUSE_EXTENSION_WORKERS_PERIOD` | `10` | Time in seconds, between each workers processing. |
