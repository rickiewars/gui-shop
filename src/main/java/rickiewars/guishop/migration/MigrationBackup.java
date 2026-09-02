package rickiewars.guishop.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class MigrationBackup {
    private MigrationBackup() {}

    public static void backup(Path legacyFile) throws IOException {
        if (!Files.exists(legacyFile)) return;

        Path backup = legacyFile.resolveSibling(legacyFile.getFileName() + ".pre-migration-backup");
        if (Files.exists(backup)) return;

        Files.copy(legacyFile, backup, StandardCopyOption.COPY_ATTRIBUTES);
    }
}
