/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.model.FileSystemErrno
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.readAttributes
import kotlin.reflect.KClass

internal fun Path.readBasicAttributes(
    followSymlinks: Boolean,
): Either<ReadAttributesError, BasicFileAttributes> = readAttributesNoThrow(BasicFileAttributes::class, followSymlinks)

internal fun <A : BasicFileAttributes> Path.readAttributesNoThrow(
    klass: KClass<A>,
    followSymlinks: Boolean,
): Either<ReadAttributesError, A> {
    val linkOptions: Array<LinkOption> = asLinkOptions(followSymlinks)
    return Either.catch {
        @Suppress("SpreadOperator")
        Files.readAttributes(this, klass.java, *linkOptions)
    }.mapLeft(ReadAttributesError::fromReadAttributesError)
}

internal fun Path.readAttributeMapIfSupported(
    attributes: String,
    followSymlinks: Boolean,
): Either<ReadAttributesError, Map<String, Any?>> {
    val linkOptions: Array<LinkOption> = asLinkOptions(followSymlinks)
    return try {
        readAttributes(attributes, options = linkOptions).right()
    } catch (_: UnsupportedOperationException) {
        emptyMap<String, Any?>().right()
    } catch (@Suppress("TooGenericExceptionCaught") throwable: Throwable) {
        ReadAttributesError.fromReadAttributesError(throwable).left()
    }
}

internal sealed interface ReadAttributesError : FileSystemOperationError {
    @WasiEmscriptenHostDataModel
    public class NotSupported(
        override val message: String = "Unsupported attribute",
    ) : ReadAttributesError {
        override val errno: FileSystemErrno = FileSystemErrno.NOTSUP
    }

    @WasiEmscriptenHostDataModel
    public class IoError(
        override val message: String,
    ) : ReadAttributesError {
        override val errno: FileSystemErrno = FileSystemErrno.IO
    }

    @WasiEmscriptenHostDataModel
    public class AccessDenied(
        override val message: String,
    ) : ReadAttributesError {
        override val errno: FileSystemErrno = FileSystemErrno.ACCES
    }

    companion object {
        internal fun fromReadAttributesError(
            throwable: Throwable,
            view: String = "BasicFileAttributeView",
        ): ReadAttributesError = when (throwable) {
            is UnsupportedOperationException -> NotSupported("Can not get $view")
            is IOException -> IoError("Can not read attributes `$view`: ${throwable.message}")
            is SecurityException -> AccessDenied("Can not read attributes: ${throwable.message}")
            else -> IoError("Can not read attributes `$view`: ${throwable.message}")
        }
    }
}
