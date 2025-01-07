---
sidebar_label: 'WASI Preview 1'
sidebar_position: 3
---

# WASI Preview 1

The library implements all non-deprecated functions of the [WASI Preview 1] specification for Kotlin targets: _JVM_ (based on NIO), _macosArm64_, macosX64_, _iosArm64_, _iosX64_, _iosSimulatorArm64_, _linuxX64_, _linuxArm64_ and _mingwX64_.

The implementation passes the [WASI Testsuite] with some exceptions that should not affect the overall functionality.

To compile C++ applications, you can use Emscripten with the `STANDALONE_WASM` flag.
See [Emscripten Standalone Mode](Emscripten.md#emscripten-standalone-mode) for more details.

Refer to the sections on integration with runtimes for usage instructions.

#### Implementation Specifics

In fact, there are 4 different implementations of file system access functions.

* **JVM target**  
  Implementation based on the NIO2 Path API with manual resolution of symbolic links in paths.
* **Apple targets**  
  Implementation based on POSIX *at functions (`openat2`, `fstatat`, …) with manual resolution of symbolic links in paths. 
* **Linux targets**  
  Implementation based on POSIX *at functions and some Linux-specific calls using the [RESOLVE_BENEATH](https://man7.org/linux/man-pages/man2/openat2.2.html#:~:text=is%20as%20follows%3A-,RESOLVE_BENEATH,-Do%20not%20permit) flag. 
* **Mingw target**  
  Implementation based on the Windows API using `NtCreateFile` from *ntdll.dll* and manual resolution of symbolic links 
  in paths.

Since the implementations differ significantly, the behavior on different systems may also vary.

Current known limitations of the WASI Preview 1 implementation:

* Network sockets are not implemented.  
  The WASI Preview 1 specification does not define any functions for creating sockets. 
  Current stub implementation of the `sock_accept`, `sock_recv`, `sock_send`, and `sock_shutdown` functions returns
  an `NOTSUPP` error.
* File system operations depend heavily on the limitations of the actual file system being used.  
  For example, on Windows, file names cannot contain the characters `:` and `?`, while on Android, the maximum 
  file name length on external storage can be limited to around 255 characters.  
  The actual file system may be case-sensitive or case-insensitive and may not support symlinks or hard links.
* The implementation of [WASI filesystem path sandboxing] has not been thoroughly tested and may contain errors.  
  Do not use it if strict sandboxing is required. The JVM NIO-based filesystem implementation is based on using
  absolute paths, making it nearly impossible to guarantee path isolation.
* External changes to the file system are not tracked.  
  For example, if a directory opened inside the WebAssembly runtime is renamed outside the virtual environment,
  it will no longer be accessible when using the NIO implementation of the file system.
* Thread safety is not fully implemented.  
  Currently, all file system operations are executed under a single lock.
* Some file system operations that should be atomic may not be atomic.
* File inodes and other unique identifiers are not mangled. File system calls returns real values.
* Pagination in the `fd_readdir` function, based on the `cookie` parameter, is partially implemented.  
  Most file systems do not provide an API to implement it correctly. The Cookie-based approach was removed 
  in WASI Preview 2.
* The `fd_fdstat_set_rights` function is not implemented as it is considered deprecated and is not used anywhere.
* The `process_cputime_id` and `thread_cputime_id` clocks are not implemented on some targets (e.g., JVM) or may have low precision.
* The `poll_oneoff` implementation on JVM uses a non-optimal variant based on periodic polling, which may affect interactive applications.

[WASI Preview 1]: https://wasi.dev/
[WASI Testsuite]: https://github.com/WebAssembly/wasi-testsuite
[WASI filesystem path sandboxing]: https://github.com/WebAssembly/wasi-filesystem/blob/main/path-resolution.md
