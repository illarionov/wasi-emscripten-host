# Module bindings-graalvm241

[GraalWasm] Emscripten WebAssembly runtime integration.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-graalvm241?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-graalvm240/overview)

* Targets: *JVM*

Use [GraalvmHostFunctionInstaller] to install host functions into the GraalVM context. 

## Usage

Usage example:

 ```kotlin
const val HELLO_WORLD_MODULE_NAME: String = "helloworld"
val source = Source.newBuilder("wasm", App::class.java.getResource("helloworld.wasm"))
    .name(HELLO_WORLD_MODULE_NAME)
    .build()

// Setup Polyglot Context
val context: Context = Context.newBuilder().build()
context.use {
    // Context must be initialized before installing modules
    context.initialize("wasm")

    // Setup modules
    val installer = GraalvmHostFunctionInstaller(context) {
       ...
    }
    installer.setupWasiPreview1Module()
    val emscriptenInstaller = installer.setupEmscriptenFunctions()

    // Evaluate the WebAssembly module
    context.eval(source)

    // Finish initialization after module instantiation
    emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME).use { emscriptenEnv ->
        // Initialize Emscripten runtime environment
        emscriptenEnv.emscriptenRuntime.initMainThread()

        // Execute code
        run(context)
    }
}
 ```

[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
