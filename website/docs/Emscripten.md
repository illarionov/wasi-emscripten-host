---
sidebar_label: 'Emscripten Environment'
sidebar_position: 4
---

# Emscripten JS environment

[Emscripten] host functions are internal implementation details of the Emscripten.
They are not publicly defined or documented, and the interfaces can change with nearly every new version.
Consequently, our implementation of these host functions is designed to work only with WebAssembly files 
compiled using a specific version of Emscripten and with a specific set of compilation flags.

The currently supported version of Emscripten is [3.1.66](https://github.com/emscripten-core/emscripten/releases/tag/3.1.66).

## Emscripten Standalone Mode

Emscripten offers a "[Standalone Mode]" of compilation intended for building applications to run in WASM runtimes
without JavaScript. In this mode, the WASI APIs are used as much as possible. However, when using this mode, binaries
tend to be larger, and the WASI API may not be sufficient to implement certain functions required by the application.

For now, we are focusing on binaries compiled without Standalone Mode, with the expectation of supporting 
standalone binaries in the future.

## Example

Consider the following "Hello World" example:

```cpp title="helloworld.cpp"
#include <stdio.h>

int main() {
  printf("hello, world!\n");
  return 0;
}
```

You can compile it to run in our environment using the `emcc` command with the following parameters:

```shell
$ emcc helloworld.cpp -O3 -g -mbulk-memory -o helloworld.mjs
```

This will generate two files `helloworld.mjs` (which we don't use) and `helloworld.wasm`.

Since we are using the `-o hello.mjs` parameter, the `STANDALONE_WASM` mode is not enabled during compilation
(see the description of the [-o target][emscripten-o-target] parameter).

To ensure that the exported symbols in the generated WebAssembly binary are not mangled, we use the `-g` parameter.  
Debugging symbols can later be removed from the binary using the `wasm-strip` command from the [WABT] package.

Feel free to experiment with other compilation options. All Emscripten options can be found in
[emscripten/src/settings.js](https://github.com/emscripten-core/emscripten/blob/main/src/settings.js).

If you need to find out the memory limits declared in the WebAssembly binary, you can use the `wasm-objdump` 
from the [WABT] package:

```shell
$ wasm-objdump -x helloworld.wasm -j Memory

helloworld.wasm:	file format wasm 0x1

Section Details:

Memory[1]:
 - memory[0] pages: initial=258 max=258

```

## Implemented functions

Implemented (or declared) Emscripten functions:

```
__assert_fail
__handle_stack_overflow
__pthread_create_js
__syscall_chmod
__syscall_faccessat
__syscall_fchmod
__syscall_fchown32
__syscall_fcntl64
__syscall_fdatasync
__syscall_fstat64
__syscall_ftruncate64
__syscall_getcwd
__syscall_ioctl
__syscall_lstat64
__syscall_mkdirat
__syscall_newfstatat
__syscall_openat
__syscall_readlinkat
__syscall_rmdir
__syscall_stat64
__syscall_unlinkat
__syscall_utimensat
_abort_js
_emscripten_get_now_is_monotonic
_emscripten_init_main_thread_js
_emscripten_notify_mailbox_postmessage
_emscripten_receive_on_main_thread_js
_emscripten_thread_cleanup
_emscripten_thread_mailbox_await
_emscripten_thread_set_strongref
_localtime_js
_mmap_js
_munmap_js
_tzset_js
emscripten_asm_const_async_on_main_thread
emscripten_asm_const_int
emscripten_check_blocking_allowed
emscripten_console_error
emscripten_date_now
emscripten_exit_with_live_runtime
emscripten_get_now
emscripten_resize_heap
emscripten_unwind_to_js_event_loop
exit
getentropy
```

[Emscripten]: https://emscripten.org/

[Standalone Mode]: https://v8.dev/blog/emscripten-standalone-wasm

[WABT]: https://github.com/WebAssembly/wabt

[emscripten-o-target]: https://emscripten.org/docs/tools_reference/emcc.html#emcc-o-target
