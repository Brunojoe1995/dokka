/*
 * Copyright 2014-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.dokka.base.renderers

interface OutputWriter {

    suspend fun write(path: String, text: String, ext: String)
    suspend fun writeResources(pathFrom: String, pathTo: String)
}
