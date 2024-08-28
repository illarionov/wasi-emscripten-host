/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.multiplatform

import at.released.weh.gradle.multiplatform.publish.createWehVersionsExtension
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.dokka.gradle.DokkaTask

/*
 * Convention plugin with publishing defaults
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.vanniktech.maven.publish.base")
    id("org.jetbrains.dokka")
}

createWehVersionsExtension()

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

private val publishedMavenLocalRoot = project.rootProject.layout.buildDirectory.dir("localMaven")

mavenPublishing {
    publishing {
        repositories {
            maven {
                name = "test"
                setUrl(publishedMavenLocalRoot.map { it.dir("test") })
            }
            maven {
                name = "PixnewsS3"
                setUrl("s3://maven.pixnews.ru/")
                credentials(AwsCredentials::class) {
                    accessKey = providers.environmentVariable("YANDEX_S3_ACCESS_KEY_ID").getOrElse("")
                    secretKey = providers.environmentVariable("YANDEX_S3_SECRET_ACCESS_KEY").getOrElse("")
                }
            }
        }
    }

    signAllPublications()

    configure(
        KotlinMultiplatform(javadocJar = JavadocJar.Dokka("dokkaGfm")),
    )

    pom {
        name.set(project.name)
        description.set(
            "Kotlin Multiplatform Implementation of WebAssembly WASI Preview 1 and Emscripten host functions",
        )
        url.set("https://github.com/illarionov/wasi-emscripten-host")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("illarionov")
                name.set("Alexey Illarionov")
                email.set("alexey@0xdc.ru")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/illarionov/wasi-emscripten-host.git")
            developerConnection.set("scm:git:ssh://github.com:illarionov/wasi-emscripten-host.git")
            url.set("https://github.com/illarionov/wasi-emscripten-host")
        }
    }
}

tasks.withType<DokkaTask> {
    notCompatibleWithConfigurationCache("https://github.com/Kotlin/dokka/issues/2231")
    dokkaSourceSets.configureEach {
        skipDeprecated.set(true)
    }
}
