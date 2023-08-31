/*
 * Copyright 2014-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.dokka.base.resolvers.external

import org.jetbrains.dokka.base.resolvers.shared.ExternalDocumentation

fun interface ExternalLocationProviderFactory {
    fun getExternalLocationProvider(doc: ExternalDocumentation): ExternalLocationProvider?
}
