/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.sdk.kotlin.gradle.publishing

import kotlinx.coroutines.test.runTest
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.internal.extensions.core.extra
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublishTest {
    @Test
    fun `aws-crt-kotlin can publish Kotlin Native artifacts`() = runTest {
        val project = ProjectBuilder.builder().withName("aws-crt-kotlin").build()
        project.group = "aws.sdk.kotlin.crt"
        project.version = "1.2.3"

        project.configurePublishing("aws-crt-kotlin")

        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        publishing.publications {
            ALLOWED_PUBLICATION_NAMES.forEach {
                val jvmRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "aws.sdk.kotlin.crt"
                    version = "1.2.3"
                    artifactId = "aws-crt-kotlin"
                }
                assertTrue(isAvailableForPublication(project, jvmRuntimePublication))
            }

            ALLOWED_KOTLIN_NATIVE_PUBLICATION_NAMES.forEach {
                val nativeRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "aws.sdk.kotlin.crt"
                    version = "1.2.3"
                    artifactId = "aws-crt-kotlin"
                }
                assertTrue(isAvailableForPublication(project, nativeRuntimePublication))
            }
        }
    }

    @Test
    fun `aws-sdk-kotlin can publish Kotlin Native artifacts`() = runTest {
        val project = ProjectBuilder.builder().withName("aws-sdk-kotlin").build()
        project.group = "aws.sdk.kotlin"
        project.version = "1.2.3"

        project.configurePublishing("aws-sdk-kotlin")

        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        publishing.publications {
            ALLOWED_PUBLICATION_NAMES.forEach {
                val jvmRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "aws.sdk.kotlin"
                    version = "1.2.3"
                    artifactId = "aws-runtime"
                }
                assertTrue(isAvailableForPublication(project, jvmRuntimePublication))
            }

            ALLOWED_KOTLIN_NATIVE_PUBLICATION_NAMES.forEach {
                val nativeRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "aws.sdk.kotlin"
                    version = "1.2.3"
                    artifactId = "aws-runtime"
                }
                assertTrue(isAvailableForPublication(project, nativeRuntimePublication))
            }
        }
    }

    @Test
    fun `smithy-kotlin can publish Kotlin Native artifacts`() = runTest {
        val project = ProjectBuilder.builder().withName("aws-smithy-kotlin").build()
        project.group = "aws.smithy.kotlin"
        project.version = "1.2.3"

        project.configurePublishing("smithy-kotlin", "smithy-lang")

        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        publishing.publications {
            ALLOWED_PUBLICATION_NAMES.forEach {
                val jvmRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "aws.smithy.kotlin"
                    version = "1.2.3"
                    artifactId = "runtime"
                }
                assertTrue(isAvailableForPublication(project, jvmRuntimePublication))
            }

            ALLOWED_KOTLIN_NATIVE_PUBLICATION_NAMES.forEach {
                val nativeRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "aws.smithy.kotlin"
                    version = "1.2.3"
                    artifactId = "runtime"
                }
                assertTrue(isAvailableForPublication(project, nativeRuntimePublication))
            }
        }
    }

    @Test
    fun `users can override publication`() = runTest {
        val project = ProjectBuilder.builder().withName("aws-smithy-kotlin").build()
        project.group = "aws.sdk.kotlin"
        project.version = "1.2.3"
        project.extra.set(OVERRIDE_KOTLIN_NATIVE_GROUP_NAME_VALIDATION, "true")

        project.configurePublishing("aws-sdk-kotlin", "aws")

        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        publishing.publications {
            ALLOWED_PUBLICATION_NAMES.forEach {
                val jvmRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "foo.bar.baz"
                    version = "1.2.3"
                    artifactId = "runtime"
                }
                assertTrue(isAvailableForPublication(project, jvmRuntimePublication))
            }

            ALLOWED_KOTLIN_NATIVE_PUBLICATION_NAMES.forEach {
                val nativeRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "foo.bar.baz"
                    version = "1.2.3"
                    artifactId = "runtime"
                }
                assertTrue(isAvailableForPublication(project, nativeRuntimePublication))
            }
        }
    }

    @Test
    fun `override only works when set to true`() = runTest {
        val project = ProjectBuilder.builder().withName("aws-smithy-kotlin").build()
        project.group = "aws.sdk.kotlin"
        project.version = "1.2.3"
        project.extra.set(OVERRIDE_KOTLIN_NATIVE_GROUP_NAME_VALIDATION, "this is not true")

        project.configurePublishing("aws-sdk-kotlin", "aws")

        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        publishing.publications {
            ALLOWED_PUBLICATION_NAMES.forEach {
                val jvmRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "foo.bar.baz"
                    version = "1.2.3"
                    artifactId = "runtime"
                }
                assertTrue(isAvailableForPublication(project, jvmRuntimePublication))
            }

            ALLOWED_KOTLIN_NATIVE_PUBLICATION_NAMES.forEach {
                val nativeRuntimePublication = create(it, MavenPublication::class.java).apply {
                    groupId = "foo.bar.baz"
                    version = "1.2.3"
                    artifactId = "runtime"
                }
                assertFalse(isAvailableForPublication(project, nativeRuntimePublication))
            }
        }
    }

    @Test
    fun `smithy-kotlin and aws-sdk-kotlin can publish Kotlin Wasm artifacts`() = runTest {
        listOf("aws.smithy.kotlin", "aws.sdk.kotlin").forEach { group ->
            val project = ProjectBuilder.builder().withName("runtime").build()
            project.group = group
            project.version = "1.2.3"

            project.configurePublishing("smithy-kotlin", "smithy-lang")

            val publishing = project.extensions.getByType(PublishingExtension::class.java)
            publishing.publications {
                val wasmPublication = create("wasmJs", MavenPublication::class.java).apply {
                    groupId = group
                    version = "1.2.3"
                    artifactId = "runtime-wasm-js"
                }
                assertTrue(isAvailableForPublication(project, wasmPublication), "wasmJs publication skipped for $group")
            }
        }
    }

    @Test
    fun `Kotlin Wasm artifacts outside allowed groups are not published`() = runTest {
        val project = ProjectBuilder.builder().withName("runtime").build()
        project.group = "foo.bar.baz"
        project.version = "1.2.3"

        project.configurePublishing("smithy-kotlin", "smithy-lang")

        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        publishing.publications {
            val wasmPublication = create("wasmJs", MavenPublication::class.java).apply {
                groupId = "foo.bar.baz"
                version = "1.2.3"
                artifactId = "runtime-wasm-js"
            }
            assertFalse(isAvailableForPublication(project, wasmPublication))
        }
    }

    @Test
    fun `klib type is added to native and wasm platform dependencies only`() {
        val project = ProjectBuilder.builder().withName("runtime").build()
        val pom = groovy.util.Node(null, "project")
        val deps = pom.appendNode("dependencies")
        listOf("runtime-core-wasm-js", "runtime-core-linuxx64", "runtime-core", "runtime-core-jvm").forEach {
            deps.appendNode("dependency").appendNode("artifactId", it)
        }
        val alreadyTyped = deps.appendNode("dependency")
        alreadyTyped.appendNode("artifactId", "kotlin-stdlib-wasm-js")
        alreadyTyped.appendNode("type", "klib")

        addKlibTypeToDependencies(project, "wasmJs", pom)

        val types = deps.children().associate { dep ->
            val node = dep as groovy.util.Node
            val artifactId = ((node.get("artifactId") as groovy.util.NodeList).first() as groovy.util.Node).text()
            artifactId to (node.get("type") as groovy.util.NodeList).map { (it as groovy.util.Node).text() }
        }
        assertEquals(
            mapOf(
                "runtime-core-wasm-js" to listOf("klib"),
                "runtime-core-linuxx64" to listOf("klib"),
                "runtime-core" to emptyList(),
                "runtime-core-jvm" to emptyList(),
                "kotlin-stdlib-wasm-js" to listOf("klib"),
            ),
            types,
        )
    }
}
