/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.dsl

import arrow.core.Either
import arrow.core.getOrElse
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.FileSystemInterceptor.Chain
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.preopened.PreopenedDirectory

@WasiEmscriptenHostDsl
public class FileSystemConfigBlock<E : FileSystemEngineConfig> {
    private val _interceptors: MutableList<FileSystemInterceptor> = mutableListOf()
    internal val interceptors: List<FileSystemInterceptor> get() = _interceptors

    internal var engineConfig: E.() -> Unit = {}
        private set

    internal var stdioConfig: StandardInputOutputConfigBlock.() -> Unit = {}
        private set

    public var unrestricted: Boolean = false
    public var currentWorkingDirectory: CurrentWorkingDirectoryConfig = CurrentWorkingDirectoryConfig.Default
    private val _preopenedDirectories: MutableList<PreopenedDirectory> = mutableListOf()
    public val preopenedDirectories: List<PreopenedDirectory> get() = _preopenedDirectories

    public fun addInterceptor(interceptor: FileSystemInterceptor) {
        _interceptors += interceptor
    }

    public inline fun addInterceptor(
        crossinline block: (chain: Chain<Any, FileSystemOperationError, Any>) -> Either<FileSystemOperationError, *>,
    ): Unit = addInterceptor(
        object : FileSystemInterceptor {
            override fun <I : Any, E : FileSystemOperationError, R : Any> intercept(
                chain: Chain<I, E, R>,
            ): Either<E, R> {
                @Suppress("UNCHECKED_CAST")
                return block(chain as Chain<Any, FileSystemOperationError, Any>) as Either<E, R>
            }
        },
    )

    public fun engine(block: E.() -> Unit) {
        val oldConfig = engineConfig
        engineConfig = {
            oldConfig()
            block()
        }
    }

    public fun stdio(block: StandardInputOutputConfigBlock.() -> Unit) {
        val oldConfig = stdioConfig
        stdioConfig = {
            oldConfig()
            block()
        }
    }

    public fun addPreopenedDirectory(
        realPath: String,
        virtualPath: String,
    ) {
        val virtualPathInstance = VirtualPath.create(virtualPath).getOrElse {
            error("Invalid virtual path. The path `$virtualPath` must be a Unix-like path")
        }
        _preopenedDirectories.add(PreopenedDirectory(realPath, virtualPathInstance))
    }

    public fun preopened(
        block: MutableList<PreopenedDirectory>.() -> Unit,
    ): Unit = block(_preopenedDirectories)
}
