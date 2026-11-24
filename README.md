# `awesome-os/ghost-jvm` — Zero-Gradle, Zero-Fat-JAR Ghost CAS Loader for Java & Kotlin

> **Eliminates:** Classpath Hell, `NoSuchMethodError`, 100 MB shaded Uber-JARs, slow Maven/Gradle daemons.  
> **Canonical Store:** [`awesome-os/b8g-ghost @ cas-v1`](https://github.com/awesome-os/b8g-ghost/releases/tag/cas-v1) (`manifests/index.json`)

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
