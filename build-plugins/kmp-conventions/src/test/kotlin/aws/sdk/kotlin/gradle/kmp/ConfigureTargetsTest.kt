/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.sdk.kotlin.gradle.kmp

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConfigureTargetsTest {
    private val root: File = Files.createTempDirectory("kmp-conventions-test").toFile()

    @AfterTest
    fun cleanup() {
        root.deleteRecursively()
    }

    private fun projectDir(path: String, vararg dirs: String, gradleProperties: String? = null): File =
        File(root, path).also { dir ->
            dirs.forEach { File(dir, it).mkdirs() }
            dir.mkdirs()
            gradleProperties?.let { File(dir, "gradle.properties").writeText(it) }
        }

    private fun project(dir: File, parent: Project? = null): Project =
        ProjectBuilder.builder().withProjectDir(dir).withName(dir.name).apply { parent?.let(::withParent) }.build()

    @Test
    fun `wasmJs is not configured for a common project by default`() {
        val project = project(projectDir("a", "common"))
        assertTrue(project.hasJvm)
        assertFalse(project.hasWasmJs)
    }

    @Test
    fun `wasmJs is configured when the project has a wasmJs source directory`() {
        assertTrue(project(projectDir("a", "common", "wasmJs")).hasWasmJs)
    }

    @Test
    fun `wasmJs is configured when the project opts in in its own gradle properties`() {
        val project = project(projectDir("a", "common", gradleProperties = "aws.kotlin.wasmJs=true\n"))
        assertTrue(project.hasWasmJs)
    }

    @Test
    fun `wasmJs opt-in property must be true`() {
        val project = project(projectDir("a", "common", gradleProperties = "aws.kotlin.wasmJs=yes\n"))
        assertFalse(project.hasWasmJs)
    }

    @Test
    fun `wasmJs opt-in is not inherited by child projects`() {
        // ProjectBuilder does not load gradle.properties into project properties, so model inheritance with an extra
        // property on the parent, which child projects do see.
        val parent = project(projectDir("parent", "common", gradleProperties = "aws.kotlin.wasmJs=true\n"))
        parent.extensions.extraProperties.set("aws.kotlin.wasmJs", "true")
        val child = project(projectDir("parent/child", "common"), parent)
        val container = project(projectDir("parent/container"), parent)

        assertTrue(parent.hasWasmJs)
        assertTrue(child.findProperty("aws.kotlin.wasmJs") == "true")
        assertFalse(child.hasWasmJs)
        assertFalse(container.hasWasmJs)
        assertFalse(container.needsKmpConfigured)
    }

    @Test
    fun `wasmJs opt-in property without common sources does not configure KMP`() {
        val container = project(projectDir("container", gradleProperties = "aws.kotlin.wasmJs=true\n"))
        assertFalse(container.hasWasmJs)
        assertFalse(container.needsKmpConfigured)
    }

    @Test
    fun `wasmJs opt-in is not read from project or extra properties`() {
        val project = project(projectDir("a", "common"))
        project.extensions.extraProperties.set("aws.kotlin.wasmJs", "true")
        assertFalse(project.hasWasmJs)
    }

    @Test
    fun `nonJvm source directory marks the project for KMP configuration`() {
        val project = project(projectDir("a", "nonJvm"))
        assertTrue(project.hasNonJvm)
        assertTrue(project.needsKmpConfigured)
        assertFalse(project(projectDir("b", "common")).hasNonJvm)
    }

    @Test
    fun `Chrome runs without its sandbox only for root`() {
        assertTrue(chromeNeedsNoSandbox(0))
        assertFalse(chromeNeedsNoSandbox(1000))
        assertFalse(chromeNeedsNoSandbox(null))
    }

    @Test
    fun `process uid is read on Linux`() {
        if (!System.getProperty("os.name").startsWith("Linux")) {
            assertNull(processUid())
            return
        }
        // a file this process creates is owned by its (effective) uid
        val created = File(root, "owned-by-this-process").apply { writeText("") }
        assertEquals(Files.getAttribute(created.toPath(), "unix:uid"), processUid())
    }
}
