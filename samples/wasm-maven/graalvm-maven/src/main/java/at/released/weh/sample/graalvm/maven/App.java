package at.released.weh.sample.graalvm.maven;

import at.released.weh.bindings.graalvm241.GraalvmHostFunctionInstaller;
import java.io.IOException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public class App {
    private static final String HELLO_WORLD_MODULE_NAME = "helloworld";

    public static void main(String[] args) throws IOException {
        new App().start();
    }

    public void start() throws IOException {
        // Prepare source
        var source = Source.newBuilder("wasm", App.class.getResource("helloworld.wasm"))
                .name(HELLO_WORLD_MODULE_NAME)
                .build();

        // Setup Polyglot Context
        try (var context = Context.newBuilder("wasm").build()) {
            // Context must be initialized before installing modules
            context.initialize("wasm");

            // Setup modules
            var installer = new GraalvmHostFunctionInstaller.Builder(context).build();
            installer.setupWasiPreview1Module();
            var emscriptenInstaller = installer.setupEmscriptenFunctions();

            // Evaluate the WebAssembly module
            context.eval(source);

            // Finish initialization after module instantiation
            try (var emscriptenEnvironment = emscriptenInstaller.finalize(HELLO_WORLD_MODULE_NAME)) {
                // Initialize Emscripten runtime environment
                emscriptenEnvironment.getEmscriptenRuntime().initMainThread();

                // Execute code
                executeWasmCode(context);
            }
        }
    }

    private void executeWasmCode(Context context) {
        var main = context.getBindings("wasm").getMember(HELLO_WORLD_MODULE_NAME).getMember("main");
        main.execute(/* argc */ 0, /* argv */ 0).asInt();
    }
}
