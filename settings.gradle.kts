plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "microservice"
include("shared")
include("user-service")
include("order-service")