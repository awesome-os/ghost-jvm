import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Zero-dependency Java 21+ Ghost CAS Resolver for awesome-os/b8g-ghost @ cas-v1.
 * Hydrates native libraries & isolated JARs into the shared Machine CAS with FIPS 180-4 SHA-256 verification.
 */
public final class GhostCas {
    public static final String INDEX_URL =
        "https://raw.githubusercontent.com/awesome-os/b8g-ghost/main/manifests/index.json";
    public static final String CAS_BASE =
        "https://github.com/awesome-os/b8g-ghost/releases/download/cas-v1/";

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build();

    public static Path machineCasDir() {
        String env = System.getenv("B8G_CAS_DIR");
        if (env != null && !env.isBlank()) return Path.of(env);
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String local = System.getenv("LOCALAPPDATA");
            return Path.of(local != null ? local : System.getProperty("user.home"), "AwesomeOS", "cas");
        }
        return Path.of(System.getProperty("user.home"), ".local", "share", "awesome-os", "cas");
    }

    public static String platformFilename(String baseName) {
        if (baseName.contains(".")) return baseName;
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase().contains("aarch64") ? "arm64" : "x64";
        if (os.contains("win")) return baseName + "-win64.dll";
        if (os.contains("mac")) return baseName + "-macos-" + arch + ".dylib";
        return baseName + "-linux-" + arch + ".so";
    }

    public static String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(data));
    }

    public static Path resolve(String baseName) throws Exception {
        String filename = platformFilename(baseName);
        String indexJson = HTTP.send(
            HttpRequest.newBuilder(URI.create(INDEX_URL)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        ).body();

        Pattern p = Pattern.compile("\"" + Pattern.quote(filename) + "\"\\s*:\\s*\\{[^}]*?\"sha256\"\\s*:\\s*\"([0-9a-f]{64})\"");
        Matcher m = p.matcher(indexJson);
        if (!m.find()) {
            throw new IllegalStateException("Asset not found in canonical Ghost CAS index: " + filename);
        }
        String expectedSha = m.group(1);
        Path dir = machineCasDir().resolve(expectedSha);
        Path target = dir.resolve(filename);

        if (Files.exists(target)) {
            String actual = sha256Hex(Files.readAllBytes(target));
            if (actual.equals(expectedSha)) {
                System.out.println("[ghost-jvm] [CAS-HIT ] " + filename + " (" + expectedSha.substring(0, 16) + "...)");
                return target;
            }
        }

        byte[] bytes = HTTP.send(
            HttpRequest.newBuilder(URI.create(CAS_BASE + expectedSha)).GET().build(),
            HttpResponse.BodyHandlers.ofByteArray()
        ).body();
        String actual = sha256Hex(bytes);
        if (!actual.equals(expectedSha)) {
            throw new SecurityException("Ghost SHA-256 mismatch for " + filename);
        }
        Files.createDirectories(dir);
        Path tmp = dir.resolve(filename + ".tmp");
        Files.write(tmp, bytes);
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        System.out.println("[ghost-jvm] [HYDRATED] " + filename + " (" + bytes.length + " B, sha256=" + expectedSha.substring(0, 16) + "...)");
        return target;
    }

    public static void main(String[] args) throws Exception {
        Path p1 = resolve("libbrotli");
        Path p2 = resolve("libbrotli");
        if (!p1.equals(p2)) throw new AssertionError("Expected identical CAS path");
        Path sq = resolve("libsqlite3-wasm32-wasi.wasm");
        System.out.println("[ghost-jvm] VERIFIED! Zero Maven/Gradle/Fat-JAR -> " + p1 + " & " + sq);
    }
}
