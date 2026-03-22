package prenotazione.medica.shared.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Carica le variabili da un file {@code .env} nelle system properties prima che Spring risolva
 * {@code application.properties}. Spring Boot non supporta nativamente i file {@code .env}
 * (a differenza di molti tool Node).
 * <p>
 * Regole: non sovrascrive variabili già presenti nell'ambiente OS o come {@code -D} JVM.
 * Cerca {@code .env} nella working directory corrente, in {@code PrenotaSalute-BE/} sotto la cwd
 * o accanto al repo se la cwd è la root del modulo.
 * </p>
 */
public final class DotenvBootstrap {

    private DotenvBootstrap() {
    }

    /**
     * Se esiste un file {@code .env} in una delle directory note, ne importa le entry come system properties.
     */
    public static void apply() {
        Path envDir = resolveEnvDirectory();
        if (envDir == null) {
            return;
        }
        Dotenv dotenv = Dotenv.configure()
                .directory(envDir.toString())
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, entry.getValue());
            }
        });
    }

    private static Path resolveEnvDirectory() {
        String userDir = System.getProperty("user.dir");
        List<Path> bases = new ArrayList<>();
        bases.add(Path.of(userDir));
        bases.add(Path.of(userDir, "PrenotaSalute-BE"));
        Path parent = Path.of(userDir).getParent();
        if (parent != null) {
            bases.add(parent.resolve("PrenotaSalute-BE"));
        }
        for (Path base : bases) {
            Path normalized = base.toAbsolutePath().normalize();
            Path envFile = normalized.resolve(".env");
            if (Files.isRegularFile(envFile)) {
                return normalized;
            }
        }
        return null;
    }
}
