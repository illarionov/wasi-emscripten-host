/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.test.fixtures.stdio

import at.released.weh.filesystem.stdio.StdioSource
import kotlinx.io.Buffer
import kotlinx.io.writeString

public class TestSourceProvider : StdioSource.Provider {
    public val source: TestRawSource = TestRawSource()

    override fun open(): StdioSource = source

    public class TestRawSource : StdioSource {
        public val buffer: Buffer = Buffer()

        override fun close(): Unit = Unit

        override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
            return buffer.readAtMostTo(sink, byteCount)
        }
    }

    public companion object {
        public operator fun invoke(input: String): TestSourceProvider =
            TestSourceProvider().apply {
                source.buffer.writeString(input)
            }
        public operator fun invoke(input: ByteArray): TestSourceProvider =
            TestSourceProvider().apply {
                source.buffer.write(input)
            }
    }
}
