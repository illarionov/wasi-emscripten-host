/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.ext

import kotlin.reflect.KProperty

private val MEMBER_NOT_INITIALIZED = Any()

internal class SinglePropertyThreadLocalLazyValue<E : Any?>(
    private val memberInitializer: (String) -> E,
) {
    private val memberStorage = ThreadLocal.withInitial { MEMBER_NOT_INITIALIZED }

    @Volatile
    private var propertyName: String = ""

    public fun get(property: KProperty<*>): E {
        val cachedValue = memberStorage.get()
        return if (cachedValue != MEMBER_NOT_INITIALIZED) {
            @Suppress("UNCHECKED_CAST")
            cachedValue as E
        } else {
            propertyName = property.name
            memberInitializer(property.name).also {
                memberStorage.set(it)
            }
        }
    }
}
