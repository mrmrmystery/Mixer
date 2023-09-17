plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.somewhatcity"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven ("https://maven.maxhenkel.de/repository/public")
    maven ("https://repo.codemc.io/repository/maven-public/")
    maven ("https://jitpack.io")
}

dependencies {
    paperDevBundle("1.20-R0.1-SNAPSHOT")
    implementation("de.maxhenkel.voicechat:voicechat-api:2.4.11")
    implementation("dev.arbjerg:lavaplayer:2.0.1")
    implementation("dev.jorel:commandapi-bukkit-shade:9.0.3")
    implementation("de.tr7zw:item-nbt-api:2.11.3")
}

tasks {

    shadowJar {
        relocate("dev.jorel.commandapi", "net.somewhatcity.mixer.commandapi")
        relocate("de.tr7zw.changeme.nbtapi", "net.somewhatcity.mixer.item-nbt-api")
        dependencies {
            exclude(dependency("de.maxhenkel.voicechat:voicechat-api:2.4.11"))
        }

        doLast() {
            copy {
                from(shadowJar)
                into("./testserver/plugins")
            }
        }
    }

    assemble {
        dependsOn(reobfJar)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
