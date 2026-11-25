# `awesome-os/ghost-jvm` — Zero-Gradle, Zero-Fat-JAR Ghost CAS Loader for Java & Kotlin

> **Eliminates:** Classpath Hell, `NoSuchMethodError`, 100 MB shaded Uber-JARs, slow Maven/Gradle daemons.  
> **Canonical Store:** [`awesome-os/b8g-ghost @ cas-v1`](https://github.com/awesome-os/b8g-ghost/releases/tag/cas-v1) (`manifests/index.json`)

## Quick Install & Usage

### Install via `pnpm` (or release tarball)

```powershell
pnpm add github:awesome-os/ghost-jvm
```

Or download the latest SHA-256 verified archive from [`Releases`](https://github.com/awesome-os/ghost-jvm/releases/latest).

### Usage & Verification

```powershell
# Compile and run directly with Java 21+ (zero Maven / Gradle)
javac GhostCas.java; java GhostCas
```

## 1-Line Usage (Pure Java 21 Stdlib — Zero Maven, Zero Gradle)

```java
// Hydrate any native 5-OS shard or isolated JAR from canonical O(1) cas-v1 with SHA-256 verification:
Path brotli = GhostCas.resolve("libbrotli");   // -> ~/.local/share/awesome-os/cas/<sha256>/libbrotli-linux-x64.so
Path sqlite = GhostCas.resolve("libsqlite3");  // -> shared Machine CAS (0 ms hit across Java, Python, Node, C++, lo!)
```

Run live with zero build tool:
```bash
javac GhostCas.java && java GhostCas
```

## Quick Install & Run (Windows / Linux / macOS)

```sh
# Install / hydrate from v11 CAS via holo-pm
holo-pm install repository://github.com/awesome-os/ghost-jvm@main

# Or project & run directly via holo-project (0-byte hardlink)
holo-project "project://cpp/ghost-jvm?from=repository://github.com/awesome-os/ghost-jvm@main"
```
