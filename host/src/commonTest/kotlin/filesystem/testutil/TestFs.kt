/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.testutil.TestFs.FsNode.Directory
import kotlin.jvm.JvmInline
import kotlin.test.fail

internal class TestFs {
    val root: FsNode = Directory(null, "preopen".asNodeName())
    val rootHandle = Handle(0, root)
    var lastHandle = 0
    var handles: MutableMap<Int, Handle> = mutableMapOf(rootHandle.id to rootHandle)

    fun mkDir(
        path: String,
        base: Handle = rootHandle,
        block: (Handle.() -> Unit)? = null,
    ) {
        val directory = mkdir(path.split('/').map { it.asNodeName() }, base)
        if (block != null) {
            allocateHandle(directory).use { directoryHandle ->
                block(directoryHandle)
            }
        }
    }

    fun mkFile(
        path: String,
        content: String = "$path content",
        base: Handle = rootHandle,
    ) {
        val components = path.split('/').map { it.asNodeName() }
        val filename = components.last()
        val parent = mkdir(components.dropLast(1), base)

        if (parent.nodes.containsKey(filename)) {
            fail("File already exists")
        }
        parent.nodes[filename] = FsNode.File(parent, filename, content)
    }

    fun mkSymlink(
        path: String,
        target: String,
        base: Handle = rootHandle,
    ) {
        val components = path.split('/').map { it.asNodeName() }
        val filename = components.last()
        val parent = mkdir(components.dropLast(1), base)
        if (parent.nodes.containsKey(filename)) {
            fail("File already exists")
        }
        parent.nodes[filename] = FsNode.Symlink(parent, filename, target)
    }

    private fun mkdir(
        path: List<Name>,
        base: Handle = rootHandle,
    ): Directory {
        var parent: Directory = base.node as? Directory ?: fail("node is not a directory")
        path.forEach { name ->
            val newParent = parent.nodes.getOrPut(name) { Directory(parent, name) }
            parent = newParent as? Directory ?: fail("$name is not a directory")
        }
        return parent
    }

    fun openComponent(
        base: Handle,
        component: String,
    ): Either<OpenError, Handle> = either {
        val parentNode = base.node as? Directory ?: raise(NotDirectory("$base is not a directory"))

        val isDirectoryRequest = component.endsWith('/')
        val cleanName = if (isDirectoryRequest) {
            component.dropLast(1)
        } else {
            component
        }

        if (component.isEmpty()) {
            raise(InvalidArgument("Empty path"))
        } else if (component == "..") {
            raise(NotCapable("Trying to escape directory"))
        } else if (!cleanName.all { it != '/' && it != 0.toChar() }) {
            raise(InvalidArgument("Only single component of path can be opened"))
        }

        val name = cleanName.asNodeName()
        val newNode = if (name == ".".asNodeName()) {
            parentNode
        } else {
            parentNode.nodes[name] ?: raise(NoEntry("$name not found"))
        }

        if (isDirectoryRequest && newNode is FsNode.File) {
            raise(NotDirectory("$name is a file"))
        }
        return allocateHandle(newNode).right()
    }

    fun close(handle: Handle) {
        if (handle.id == 0) {
            fail("Trying to close root handle")
        }
        val removed = handles.remove(handle.id)
        if (removed == null || removed != handle) {
            fail("Close handle `$handle` failed. Removed handle: $removed")
        }
    }

    private fun allocateHandle(node: FsNode): Handle = Handle(++lastHandle, node).also {
        handles[it.id] = it
    }

    fun allHandlesAreClosed(): Boolean {
        return handles == mapOf(0 to rootHandle)
    }

    private fun String.asNodeName(): Name = Name(this)

    sealed class FsNode(
        open var parent: Directory?,
        open val name: Name,
    ) {
        data class Directory(
            override var parent: Directory?,
            override val name: Name,
            val nodes: MutableMap<Name, FsNode> = mutableMapOf(),
        ) : FsNode(parent, name) {
            override fun toString(): String {
                return "Directory(name=$name, parent=${parent?.name}, nodes=${nodes.keys.joinToString(",")})"
            }
        }

        data class Symlink(
            override var parent: Directory?,
            override val name: Name,
            val target: String,
        ) : FsNode(parent, name) {
            override fun toString(): String {
                return "Symlink(name=$name, parent=${parent?.name}, target='$target')"
            }
        }

        data class File(
            override var parent: Directory?,
            override val name: Name,
            val content: String = "",
        ) : FsNode(parent, name) {
            override fun toString(): String {
                return "File(content='$content', parent=${parent?.name}, name=$name)"
            }
        }
    }

    inner class Handle(
        val id: Int,
        val node: FsNode,
    ) : AutoCloseable {
        val type: HandleType = when (node) {
            is Directory -> HandleType.DIRECTORY
            is FsNode.File -> HandleType.FILE
            is FsNode.Symlink -> HandleType.SYMLINK
        }

        val fullpath: String
            get() {
                var components = buildList<Name> {
                    var current: FsNode? = node
                    while (current?.parent != null) {
                        add(current.name)
                        current = current.parent
                    }
                }.reversed()
                return components.joinToString("/")
            }

        fun mkFile(
            path: String,
            content: String = "$path content",
        ) = this@TestFs.mkFile(path, content, this)

        fun mkSymlink(
            path: String,
            target: String,
        ) = this@TestFs.mkSymlink(path, target, this)

        fun mkDir(
            path: String,
            block: (Handle.() -> Unit)? = null,
        ): Unit = this@TestFs.mkDir(path, this, block)

        override fun close() {
            this@TestFs.close(this)
        }
    }

    public enum class HandleType {
        DIRECTORY,
        SYMLINK,
        FILE,
    }

    @JvmInline
    value class Name(
        val value: String,
    ) {
        init {
            check(value.all { it != '/' && it != 0.toChar() })
        }

        override fun toString(): String = value
    }
}
