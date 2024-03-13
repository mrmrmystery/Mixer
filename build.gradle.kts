plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.modrinth.minotaur") version "2.+"
}

group = "net.somewhatcity"
version = "1.0.3"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven ("https://maven.maxhenkel.de/repository/public")
    maven ("https://repo.codemc.io/repository/maven-public/")
    maven ("https://jitpack.io")
    maven {
        name = "arbjergDevSnapshots"
        url = uri("https://maven.lavalink.dev/snapshots")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("de.maxhenkel.voicechat:voicechat-api:2.4.11")
    //implementation("dev.arbjerg:lavaplayer:2.0.1")
    implementation("dev.arbjerg:lavaplayer:727959e9f621fc457b3a5adafcfffb55fdeaa538-SNAPSHOT")
    implementation("dev.jorel:commandapi-bukkit-shade:9.3.0")
    implementation("de.tr7zw:item-nbt-api:2.12.2")
    implementation("org.apache.commons:commons-math3:3.6.1")

}

tasks {
    shadowJar {
        relocate("dev.jorel.commandapi", "net.somewhatcity.mixer.commandapi")
        relocate("de.tr7zw.changeme.nbtapi", "net.somewhatcity.mixer.item-nbt-api")
        dependencies {
            exclude(dependency("de.maxhenkel.voicechat:voicechat-api:2.4.11"))
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("ThaMLsde")
    versionNumber.set(version.toString())
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(listOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4"))
    loaders.addAll(listOf("paper", "purpur"))
    dependencies {
        required.project("9eGKb6K1")
    }
}
