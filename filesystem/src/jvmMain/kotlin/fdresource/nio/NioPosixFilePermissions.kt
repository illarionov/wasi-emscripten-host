/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.ext.fileModeToPosixFilePermissions
import at.released.weh.filesystem.model.FileMode
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.UserPrincipalNotFoundException
import kotlin.io.path.fileAttributesView
import kotlin.io.path.setPosixFilePermissions

internal fun nioSetPosixFilePermissions(
    path: Path,
    @FileMode mode: Int,
): Either<ChmodError, Unit> = Either.catch {
    path.setPosixFilePermissions(mode.fileModeToPosixFilePermissions())
    Unit
}.mapLeft {
    when (it) {
        is UnsupportedOperationException -> PermissionDenied("Read-only channel")
        is ClassCastException -> InvalidArgument("Invalid flags")
        is IOException -> IoError("I/O exception: ${it.message}")
        is SecurityException -> AccessDenied("Security Exception")
        else -> throw IllegalStateException("Unexpected error", it)
    }
}

internal fun nioSetPosixUserGroup(
    path: Path,
    owner: Int,
    group: Int,
): Either<ChownError, Unit> = Either.catch {
    val lookupService = path.fileSystem.userPrincipalLookupService
    val ownerPrincipal = lookupService.lookupPrincipalByName(owner.toString())
    val groupPrincipal = lookupService.lookupPrincipalByGroupName(group.toString())
    path.fileAttributesView<PosixFileAttributeView>().run {
        setOwner(ownerPrincipal)
        setGroup(groupPrincipal)
    }
}.mapLeft {
    when (it) {
        is UserPrincipalNotFoundException -> InvalidArgument("User not exists: ${it.message}")
        is UnsupportedOperationException -> NotSupported("Operation not supported: ${it.message}")
        is IOException -> IoError("I/O error: ${it.message}")
        else -> throw IllegalStateException("Unexpected error", it)
    }
}
