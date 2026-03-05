plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellijPlatform)
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion").get())

    }
}

intellijPlatform {
    pluginConfiguration {
        id = providers.gradleProperty("pluginId")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
        val whatsNewFile = rootProject.file(".whats_new.html")
        if (whatsNewFile.exists()) {
            changeNotes = providers.provider { whatsNewFile.readText() }
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
    jarSearchableOptions {
        enabled = false
    }
    processResources {
        from("assets/ddcSoftwaresThemesIcon.svg") {
            into("META-INF")
            rename { "pluginIcon.svg" }
        }
    }
}
