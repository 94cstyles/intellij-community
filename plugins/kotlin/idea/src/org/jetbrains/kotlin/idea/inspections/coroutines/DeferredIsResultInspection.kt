// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.inspections.coroutines

import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

class DeferredIsResultInspection : AbstractIsResultInspection(
    typeShortName = "Deferred",
    typeFullName = "kotlinx.coroutines.Deferred",
    allowedSuffix = "Async",
    allowedNames = setOf("async"),
    suggestedFunctionNameToCall = "await",
    shouldMakeSuspend = true,
    simplify = fun(expression: KtExpression) {
        val qualifiedExpression = expression as? KtQualifiedExpression ?: return
        val redundantAsyncInspection = RedundantAsyncInspection()
        val conversion = redundantAsyncInspection.generateConversion(qualifiedExpression) ?: return
        redundantAsyncInspection.generateFix(conversion).apply(qualifiedExpression)
    }
)