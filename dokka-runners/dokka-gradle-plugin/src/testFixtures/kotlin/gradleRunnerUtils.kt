/*
 * Copyright 2014-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package org.jetbrains.dokka.gradle.utils

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/** Edit environment variables in the Gradle Runner */
@Deprecated("Windows does not support withEnvironment - https://github.com/gradle/gradle/issues/23959")
fun GradleRunner.withEnvironment(build: MutableMap<String, String?>.() -> Unit): GradleRunner {
    val env = environment ?: mutableMapOf()
    env.build()
    return withEnvironment(env)
}


@OptIn(ExperimentalContracts::class)
inline fun GradleRunner.build(
    handleResult: BuildResult.() -> Unit
) {
    contract { callsInPlace(handleResult, InvocationKind.EXACTLY_ONCE) }
    build().let(handleResult)
}


inline fun GradleRunner.buildAndFail(
    handleResult: BuildResult.() -> Unit
): Unit = buildAndFail().let(handleResult)


fun GradleRunner.updateGradleProperties(
    arguments: GradleProjectTest.GradleProperties,
): GradleRunner {
    val gradlePropertiesFile = projectDir.resolve("gradle.properties").apply {
        if (!exists()) {
            parentFile.mkdirs()
            createNewFile()
        }
    }

    val gradleProperties = Properties()
        .loadFile(gradlePropertiesFile)
        .entries.associate { it.key.toString() to it.value.toString() }.toMutableMap()

    arguments.toGradleProperties().forEach { (k, v) ->
        gradleProperties[k] = v
    }

    gradlePropertiesFile.writeText(
        gradleProperties
            .entries
            .sortedBy { it.key }
            .joinToString("\n", postfix = "\n") { (k, v) -> "$k=$v" }
    )

    return this
}

fun GradleRunner.updateGradlePropertiesV2(
    arguments: GradleProjectTest.GradleProperties,
): GradleRunner {
    projectDir.walk()
        .filter { it.name == "settings.gradle.kts" || it.name == "settings.gradle" }
        .map { it.resolveSibling("gradle.properties") }
        .forEach { gradlePropertiesFile ->

            gradlePropertiesFile.apply {
                if (!exists()) {
                    parentFile.mkdirs()
                    createNewFile()
                }
            }

            val gradleProperties = Properties()
                .apply { gradlePropertiesFile.inputStream().use { load(it) } }
                .entries
                .associate { it.key.toString() to it.value.toString() }
                .toMutableMap()

            arguments.toGradleProperties().forEach { (k, v) ->
                gradleProperties[k] = v
            }

            gradlePropertiesFile.writeText(
                buildString {
                    gradleProperties.entries
                        .sortedBy { it.key }
                        .forEach { (k, v) -> appendLine("$k=$v") }
                }
            )
        }

    return this
}


/**
 * Helper function to _append_ [arguments] to any existing
 * [GradleRunner arguments][GradleRunner.getArguments].
 */
fun GradleRunner.addArguments(
    vararg arguments: String
): GradleRunner = addArguments(arguments.asList())

/**
 * Helper function to _append_ [arguments] to any existing
 * [GradleRunner arguments][GradleRunner.getArguments].
 */
fun GradleRunner.addArguments(
    arguments: List<String>,
): GradleRunner =
    withArguments(this@addArguments.arguments + arguments)

/**
 * Get the name of the task, without the leading [BuildTask.getPath].
 */
val BuildTask.name: String
    get() = path.substringAfterLast(':')


private fun Properties.loadFile(file: File): Properties {
    file.reader().use { load(it) }
    return this
}
