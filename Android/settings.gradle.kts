pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://naver.jfrog.io/artifactory/maven/")
        }
        maven {
            url = uri("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/")
        }



    }
}

rootProject.name = "navigationwalkers"
include(":app")
 