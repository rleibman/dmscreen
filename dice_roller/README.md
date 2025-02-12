Dice Roller WebAssembly (Rust to WASM)
---------------------------------------

This project compiles a Rust-based dice roller into WebAssembly (WASM) so it can be used in a web browser.

# Install Dependencies
Before compiling, ensure you have the required tools installed:

## Install Rust & WebAssembly Target
```sh
rustup target add wasm32-unknown-unknown
```

## Install wasm-bindgen CLI
```sh
cargo install wasm-bindgen-cli
```

# Build for WebAssembly
Compile Rust into WebAssembly:

```sh
cargo build --release --target wasm32-unknown-unknown
```

# Generate JavaScript Bindings
Run wasm-bindgen to generate dice_parser.js:

```sh
wasm-bindgen --target web --out-dir ./wasm-out ./target/wasm32-unknown-unknown/release/dice_parser.wasm
```

Generated Files (inside wasm-out/)

dice_parser.js (JavaScript glue code)
dice_parser_bg.wasm (Compiled WASM module)
🌍 5. Create index.html for Web Interface
Create index.html in your project root:


# Run WebAssembly in a Local Server
Since WebAssembly must be served via HTTP, use:

```sh
python3 -m http.server 8000
```

http://localhost:8000/index.html

# Try It!
Enter a roll (2d6+3) in the input box.
Click Roll.
See the result! 🎲
