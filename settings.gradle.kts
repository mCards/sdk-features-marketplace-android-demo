pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

val githubUsername = providers.gradleProperty("GITHUB_USERNAME")
val githubToken = providers.gradleProperty("GITHUB_TOKEN")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/Wantsa/sdk-auth-android")
            credentials {
                username = githubUsername.get()
                password = githubToken.get()
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/Wantsa/sdk-features-marketplace-android")
            credentials {
                username = githubUsername.get()
                password = githubToken.get()
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/Wantsa/sdk-cards-android")
            credentials {
                username = githubUsername.get()
                password = githubToken.get()
            }
        }
    }
}

rootProject.name = "mCards FM SDK Demo"
include(":app")
