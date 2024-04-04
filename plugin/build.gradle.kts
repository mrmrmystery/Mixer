plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.modrinth.minotaur") version "2.+"
}

repositories {
    mavenCentral()
    maven ("https://maven.maxhenkel.de/repository/public")
    maven ("https://repo.codemc.io/repository/maven-public/")
    maven ("https://jitpack.io")
    maven {
        name = "arbjergDevSnapshots"
        url = uri("https://maven.lavalink.dev/snapshots")
    }
    maven {
        name = "TarsosDSP repository"
        url = uri("https://mvn.0110.be/releases")
    }
    maven {
        name = "henkelmax.public"
        url = uri("https://maven.maxhenkel.de/repository/public")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("de.maxhenkel.voicechat:voicechat-api:2.4.11")
    //implementation("dev.arbjerg:lavaplayer:2.0.1")
    //implementation("dev.arbjerg:lavaplayer:727959e9f621fc457b3a5adafcfffb55fdeaa538-SNAPSHOT")
    implementation("dev.arbjerg:lavaplayer:0eaeee195f0315b2617587aa3537fa202df07ddc-SNAPSHOT")

    implementation("dev.jorel:commandapi-bukkit-shade:9.3.0")
    implementation("de.tr7zw:item-nbt-api:2.12.2")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("be.tarsos.dsp:core:2.5")
    implementation("be.tarsos.dsp:jvm:2.5")
    implementation("de.maxhenkel.opus4j:opus4j:2.0.2")

    api(project(":api"))
}

tasks {
    shadowJar {
        destinationDirectory.set(rootProject.buildDir.resolve("libs"))
        archiveBaseName.set(rootProject.name)

        relocate("dev.jorel.commandapi", "net.somewhatcity.mixer.commandapi")
        relocate("de.tr7zw.changeme.nbtapi", "net.somewhatcity.mixer.item-nbt-api")
        dependencies {
            exclude(dependency("de.maxhenkel.voicechat:voicechat-api:2.4.11"))
        }
    }
}

bukkit {
    main = "$group.mixer.core.MixerPlugin"
    apiVersion = "1.19"
    authors = listOf("mrmrmystery")
    name = rootProject.name
    depend = listOf("voicechat")
    version = rootProject.version.toString()
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("ThaMLsde")
    versionNumber.set(rootProject.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(listOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4"))
    loaders.addAll(listOf("paper", "purpur"))
    dependencies {
        required.project("9eGKb6K1")
    }
}