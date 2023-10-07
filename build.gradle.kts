plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    testImplementation("junit:junit:4.11")
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
    }
    test {
        java {
            srcDir("test")
        }
    }
}

group = "com.iukonline.amule"
version = "0.5.1-SNAPSHOT"
description = "Java aMule remote control library"
java.sourceCompatibility = JavaVersion.VERSION_1_7

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
