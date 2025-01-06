/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import arrow.core.getOrElse
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.preopened.PreopenedDirectory
import kotlin.jvm.JvmSynthetic

@WasiEmscriptenHostDsl
public class FileSystemSimpleConfigBlock internal constructor() {
    /**
     * Specifies whether access to files outside the pre-opened directories is allowed.
     */
    @set:JvmSynthetic // Hide from Java
    public var unrestricted: Boolean = false

    @JvmSynthetic // Hide from Java
    internal var currentWorkingDirectoryConfig: CurrentWorkingDirectoryConfig = CurrentWorkingDirectoryConfig.Default

    /**
     * Sets the current working directory.
     * Used in Emscripten bindings.
     */
    @set:JvmSynthetic // Hide from Java
    @Suppress("NO_CORRESPONDING_PROPERTY")
    public var currentWorkingDirectory: String?
        get() = (currentWorkingDirectoryConfig as? CurrentWorkingDirectoryConfig.Path)?.path
        set(value) {
            currentWorkingDirectoryConfig = if (value != null) {
                CurrentWorkingDirectoryConfig.Path(value)
            } else {
                CurrentWorkingDirectoryConfig.Inactive
            }
        }

    private val _preopenedDirectories: MutableList<PreopenedDirectory> = mutableListOf()

    /**
     * Returns a list of pre-opened directories.
     */
    public val preopenedDirectories: List<PreopenedDirectory> get() = _preopenedDirectories.toMutableList()

    /**
     * Implementation of the filesystem.
     * Allows you to completely redefine the implementation. This provides access to more precise settings
     * and parameters of a specific implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var fileSystem: FileSystem? = null

    /**
     * Specifies whether access to files outside the pre-opened directories is allowed.
     */
    public fun setUnrestricted(unrestricted: Boolean): FileSystemSimpleConfigBlock = apply {
        this.unrestricted = unrestricted
    }

    /**
     * Sets the current working directory.
     * Used in Emscripten bindings.
     */
    public fun setCurrentWorkingDirectory(currentWorkingDirectory: String?): FileSystemSimpleConfigBlock = apply {
        this.currentWorkingDirectory = currentWorkingDirectory
    }

    /**
     * Adds a pre-opened directory.
     * The directory is mapped from a real path on the host system to a virtual path on the WASM FileSystem.
     *
     * @param realPath The path of the directory on the real file system.
     * @param virtualPath Mount point inside the virtual file system.
     */
    public fun addPreopenedDirectory(
        realPath: String,
        virtualPath: String,
    ): FileSystemSimpleConfigBlock = apply {
        val virtualPathInstance = VirtualPath.create(virtualPath).getOrElse {
            error("Invalid virtual path. The path `$virtualPath` must be a Unix-like path")
        }
        _preopenedDirectories.add(PreopenedDirectory(realPath, virtualPathInstance))
    }

    @JvmSynthetic // Hide from Java
    public fun preopened(
        block: MutableList<PreopenedDirectory>.() -> Unit,
    ): Unit = block(_preopenedDirectories)

    /**
     * Sets the filesystem implementation.
     * Allows you to completely redefine the implementation. This provides access to more precise settings
     * and parameters of a specific implementation.
     */
    public fun setFileSystem(fileSystem: FileSystem?): FileSystemSimpleConfigBlock = apply {
        this.fileSystem = fileSystem
    }
}
