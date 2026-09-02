package rickiewars.guishop.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MigrationBackup is the only safety net a user has if the rest of the migration goes wrong,
 * so its two guarantees -- "always copy" and "never clobber an existing backup" -- are pinned here.
 */
public class MigrationBackupTest {

    @TempDir
    Path dir;

    @Test
    void copiesToPreMigrationBackupBesideTheOriginal() throws IOException {
        Path legacy = dir.resolve("guishop.json");
        Files.writeString(legacy, "{\"shops\":[]}", StandardCharsets.UTF_8);

        MigrationBackup.backup(legacy);

        Path backup = dir.resolve("guishop.json.pre-migration-backup");
        assertTrue(Files.exists(backup), "backup was not created");
        assertEquals("{\"shops\":[]}", Files.readString(backup, StandardCharsets.UTF_8));
        assertTrue(Files.exists(legacy), "the original must survive the backup");
    }

    @Test
    void copyIsByteIdenticalIncludingUtf8() throws IOException {
        Path legacy = dir.resolve("guishopeconomy.json");
        byte[] content = "{\"name\":\"Münze — Café ✦\"}".getBytes(StandardCharsets.UTF_8);
        Files.write(legacy, content);

        MigrationBackup.backup(legacy);

        assertArrayEquals(content, Files.readAllBytes(dir.resolve("guishopeconomy.json.pre-migration-backup")));
    }

    @Test
    void missingSourceIsANoOp() {
        Path legacy = dir.resolve("absent.json");

        assertDoesNotThrow(() -> MigrationBackup.backup(legacy));
        assertFalse(Files.exists(dir.resolve("absent.json.pre-migration-backup")));
    }

    @Test
    void neverOverwritesAnExistingBackup() throws IOException {
        Path legacy = dir.resolve("guishop.json");
        Path backup = dir.resolve("guishop.json.pre-migration-backup");
        Files.writeString(backup, "PRISTINE ORIGINAL", StandardCharsets.UTF_8);
        Files.writeString(legacy, "half-migrated garbage", StandardCharsets.UTF_8);

        MigrationBackup.backup(legacy);

        assertEquals(
            "PRISTINE ORIGINAL",
            Files.readString(backup, StandardCharsets.UTF_8),
            "a retry after a failed boot must not overwrite the first backup"
        );
    }

    @Test
    void repeatedBackupsOfTheSameFileAreStable() throws IOException {
        Path legacy = dir.resolve("guishop.json");
        Files.writeString(legacy, "original", StandardCharsets.UTF_8);

        MigrationBackup.backup(legacy);
        Files.writeString(legacy, "changed later", StandardCharsets.UTF_8);
        MigrationBackup.backup(legacy);

        assertEquals("original", Files.readString(dir.resolve("guishop.json.pre-migration-backup"), StandardCharsets.UTF_8));
    }
}
