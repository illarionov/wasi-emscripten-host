package at.released.weh.sample.graalvm.maven;

import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller;
import at.released.weh.host.EmbedderHost;
import at.released.weh.host.EmbedderHostBuilder;
import java.io.IOException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public class App {
    private static final String HELLO_WORLD_MODULE_NAME = "helloworld";

    public static void main(String[] args) throws Exception {
        new App().start();
    }

    public void start() throws Exception {
        // Prepare source
        var source = Source.newBuilder("wasm", App.class.getResource("helloworld.wasm"))
                .name(HELLO_WORLD_MODULE_NAME)
                .build();

        // Prepare Host
        var hostBuilder = new EmbedderHostBuilder();
        hostBuilder.fileSystem().setUnrestricted(true);
        try (var embedderHost = hostBuilder.build()) {
            executeWasmCode(embedderHost, source);
        }
    }

    private void executeWasmCode(EmbedderHost embedderHost, Source wasmSource) throws IOException {
        // Setup Polyglot Context
        try (var context = Context.newBuilder("wasm").build()) {
            // Context must be initialized before installing modules
            context.initialize("wasm");

            // Setup modules
            var installer = new GraalvmHostFunctionInstaller.Builder(context).setHost(embedderHost).build();
            installer.setupWasiPreview1Module();

            var emscriptenInstaller = installer.setupEmscriptenFunctions();

            // Evaluate the WebAssembly module
            context.eval(wasmSource);

            // Finish initialization after module instantiation
            try (var emscriptenEnvironment = emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME)) {
                // Initialize Emscripten runtime environment
                emscriptenEnvironment.getEmscriptenRuntime().initMainThread();

                // Execute code
                var main = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("main");
                main.execute(/* argc */ 0, /* argv */ 0).asInt();
            }
        }
    }
}
