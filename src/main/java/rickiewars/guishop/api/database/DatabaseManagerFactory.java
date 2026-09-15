package rickiewars.guishop.api.database;

import rickiewars.guishop.api.database.impl.SQLiteDatabaseManager;
import rickiewars.guishop.config.GuiShopConfig;

public interface DatabaseManagerFactory {

    static DatabaseManager create(GuiShopConfig config) {
        assert config.database != null;
        GuiShopConfig.DatabaseConfig.DatabaseType type = config.database.type;
        return switch (type) {
            case SQLITE -> new SQLiteDatabaseManager(config);
            default -> throw new RuntimeException("Unsupported database type: " + type);
        };
    }
}
