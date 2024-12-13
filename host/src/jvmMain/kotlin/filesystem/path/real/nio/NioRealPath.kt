/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")

package at.released.weh.filesystem.path.real.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.RealPath
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import java.io.IOError
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.isDirectory

internal class NioRealPath private constructor(
    val nio: Path,
) : RealPath {
    internal val kString: String get() = nio.toString()
    override val utf8Bytes: ByteString by lazy(LazyThreadSafetyMode.PUBLICATION) {
        kString.encodeToByteString()
    }
    val isAbsolute: Boolean get() = nio.isAbsolute

    val isDirectoryRequest: Boolean = kString.last().let {
        it == '\\' || it == '/'
    }

    override fun decodeToString(): Either<PathError.InvalidPathFormat, String> {
        return nio.toString().right()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        other as NioRealPath

        return nio == other.nio
    }

    override fun hashCode(): Int {
        return nio.hashCode()
    }

    internal class NioRealPathFactory(
        private val fileSystem: FileSystem = FileSystems.getDefault(),
    ) : RealPath.Factory<NioRealPath> {
        override fun create(bytes: ByteString): Either<PathError, NioRealPath> {
            return Either.Companion.catch { bytes.toByteArray().decodeToString(throwOnInvalidSequence = true) }
                .mapLeft { PathError.InvalidPathFormat("Path is not a valid Unicode string") }
                .flatMap<PathError, String, NioRealPath>(::create)
        }

        override fun create(path: String): Either<PathError, NioRealPath> {
            // TODO: validate path
            return NioRealPath(fileSystem.getPath(path)).right()
        }

        fun create(path: Path): NioRealPath = NioRealPath(path)
    }

    internal companion object {
        fun NioRealPath.resolveAbsolutePath(): NioRealPath = NioRealPath(nio.toAbsolutePath())

        fun NioRealPath.isDirectory(): Boolean = this.nio.isDirectory()

        fun resolve(
            pathToResolve: NioRealPath,
            basePath: NioRealPath,
        ): Either<ResolvePathError, NioRealPath> {
            return Either.catch {
                NioRealPath(basePath.nio.resolve(pathToResolve.nio))
            }.mapLeft { ex ->
                when (ex) {
                    is InvalidPathException -> PathError.InvalidPathFormat("Can not resolve path `$pathToResolve`")
                    is IOError -> PathError.IoError(ex.message.toString())
                    else -> throw ex
                }
            }
        }
    }
}
