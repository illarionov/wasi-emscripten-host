/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.common

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import kotlin.concurrent.Volatile
import kotlin.reflect.KProperty

private val MEMBER_NOT_INITIALIZED = Any()

@InternalWasiEmscriptenHostApi
public class SinglePropertyLazyValue<E : Any?>(
    private val memberInitializer: (String) -> E,
) {
    @Volatile
    private var _member: Any? = MEMBER_NOT_INITIALIZED

    @Volatile
    private var _propertyName: String = ""

    public fun get(property: KProperty<*>): E = _member.let { cachedMember ->
        if (cachedMember != MEMBER_NOT_INITIALIZED) {
            require(_propertyName == property.name)
            @Suppress("UNCHECKED_CAST")
            cachedMember as E
        } else {
            _propertyName = property.name
            val member = memberInitializer(property.name)
            _member = member
            member
        }
    }
}
