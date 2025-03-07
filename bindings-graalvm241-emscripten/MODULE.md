# Module bindings-graalvm241-emscripten

Implementation of Emscripten host functions for the [GraalWasm] JVM WebAssembly runtime integration.

[<img alt="Maven Central Version" src="https://img.shields.io/maven-central/v/at.released.weh/bindings-graalvm241-emscripten?style=flat-square">](https://central.sonatype.com/artifact/at.released.weh/bindings-graalvm240-emscripten/overview)

* Targets: *JVM*

Use [GraalvmHostFunctionInstaller](https://weh.released.at/api/bindings-graalvm241-emscripten/at.released.weh.bindings.graalvm241/-graalvm-host-function-installer/index.html) to install host functions into the GraalVM context. 

## Usage

Usage example:

 ```kotlin
// Create Host and run code
EmbedderHost {
    fileSystem {
        unrestricted = true
    }
}.use(::executeCode)

private fun executeCode(embedderHost: EmbedderHost) {
    val HELLO_WORLD_MODULE_NAME: String = "helloworld"

    // Prepare Source
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
            host = embedderHost
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
            val mainFunction = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("main")
            mainFunction.execute(
                /* argc */ 0,
                /* argv */0,
            ).asInt()
        }
    }
}
 ```

[GraalWasm]: https://www.graalvm.org/latest/reference-manual/wasm/
