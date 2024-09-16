/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NO_BRACES_IN_CONDITIONALS_AND_LOOPS")

package at.released.weh.bindings.graalvm240

import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.host.base.memory.Pages
import at.released.weh.host.base.memory.WASM_MEMORY_DEFAULT_MAX_PAGES

public class MemorySpec private constructor(
    public val maxSize: Pages,
    public val minSize: Pages,
    public val sharedMemory: Boolean,
    public val useUnsafeMemory: Boolean,
    public val supportMemory64: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemorySpec

        if (maxSize != other.maxSize) return false
        if (minSize != other.minSize) return false
        if (sharedMemory != other.sharedMemory) return false
        if (useUnsafeMemory != other.useUnsafeMemory) return false
        if (supportMemory64 != other.supportMemory64) return false

        return true
    }

    override fun hashCode(): Int {
        var result = maxSize.hashCode()
        result = 31 * result + minSize.hashCode()
        result = 31 * result + sharedMemory.hashCode()
        result = 31 * result + useUnsafeMemory.hashCode()
        result = 31 * result + supportMemory64.hashCode()
        return result
    }

    @WasiEmscriptenHostDsl
    public class Builder {
        /**
         * Limits of the imported memory: minimum size in pages.
         */
        @set:JvmSynthetic
        public var minSize: Pages = Pages(0)

        /**
         * Limits of the imported memory: maximum size in pages.
         */
        @set:JvmSynthetic
        public var maxSize: Pages = WASM_MEMORY_DEFAULT_MAX_PAGES

        /**
         * Specifies whether this memory is shared
         */
        @set:JvmSynthetic
        public var shared: Boolean = false

        /**
         * If true, a memory implementation based on [sun.misc.Unsafe] is used.
         * Required when threading support is activated in the Graal Context and [shared] is true.
         */
        @set:JvmSynthetic
        public var useUnsafe: Boolean = false

        /**
         * Sets whether the memory supports 64-bit extension
         */
        @set:JvmSynthetic
        public var supportMemory64: Boolean = false

        public fun setMinSize(minSize: Pages): Builder = apply {
            this.minSize = minSize
        }

        public fun setMaxSize(maxSize: Pages): Builder = apply {
            require(maxSize.count > 0) { "maxSizePages should be positive" }
            this.maxSize = maxSize
        }

        public fun setShared(shared: Boolean): Builder = apply {
            this.shared = shared
        }

        public fun setUseUnsafe(useUnsafe: Boolean): Builder = apply {
            this.useUnsafe = useUnsafe
        }

        public fun setSupportMemory64(supportMemory64: Boolean): Builder = apply {
            this.supportMemory64 = supportMemory64
        }

        public fun build(): MemorySpec {
            require(minSize.count <= maxSize.count) { "maxSizePages should not be less than minSizePages" }

            return MemorySpec(
                minSize = minSize,
                maxSize = maxSize,
                sharedMemory = shared,
                useUnsafeMemory = useUnsafe,
                supportMemory64 = supportMemory64,
            )
        }
    }

    public companion object {
        @JvmSynthetic
        public operator fun invoke(
            block: Builder.() -> Unit = {},
        ): MemorySpec {
            return Builder().apply(block).build()
        }
    }
}
