/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.sdk.kotlin.gradle.kmp

import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigureTargetsTest {
    private fun projectWithDirs(vararg dirs: String) = Files.createTempDirectory("kmp-conventions-test").toFile().let { root ->
        dirs.forEach { File(root, it).mkdirs() }
        ProjectBuilder.builder().withProjectDir(root).build()
    }

    @Test
    fun `wasmJs is not configured for a common project by default`() {
        val project = projectWithDirs("common")
        assertTrue(project.hasJvm)
        assertFalse(project.hasWasmJs)
    }

    @Test
    fun `wasmJs is configured when the project has a wasmJs source directory`() {
        val project = projectWithDirs("common", "wasmJs")
        assertTrue(project.hasWasmJs)
    }

    @Test
    fun `wasmJs is configured when the project opts in by property`() {
        val project = projectWithDirs("common")
        project.extensions.extraProperties.set("aws.kotlin.wasmJs", "true")
        assertTrue(project.hasWasmJs)
    }

    @Test
    fun `wasmJs opt-in property must be true`() {
        val project = projectWithDirs("common")
        project.extensions.extraProperties.set("aws.kotlin.wasmJs", "yes")
        assertFalse(project.hasWasmJs)
    }

    @Test
    fun `nonJvm source directory marks the project for KMP configuration`() {
        val project = projectWithDirs("nonJvm")
        assertTrue(project.hasNonJvm)
        assertTrue(project.needsKmpConfigured)
        assertFalse(projectWithDirs("common").hasNonJvm)
    }
}
