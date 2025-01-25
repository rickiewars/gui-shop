package rickiewars.guishop.serializer;

import com.google.gson.*;
import rickiewars.guishop.config.Config.DatabaseConfig;
import rickiewars.guishop.config.Config.DatabaseConfig.DatabaseType;

import java.lang.reflect.Type;

public class DatabaseConfigSerializer implements JsonSerializer<DatabaseConfig>, JsonDeserializer<DatabaseConfig> {
    @Override
    public DatabaseConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject database = jsonElement.getAsJsonObject();

        return this.deserializeAsSqlite(database);

        // For future reference
//        String typeString = database.has("type")
//                ? database.get("type").getAsString()
//                : DatabaseType.SQLITE.name;
//
//        if (typeString.equals(DatabaseType.MYSQL.name)) {
//            // for future reference
//            return this.deserializeAsMysql(account);
//        } else {
//            GUIShop.LOGGER.warn("Invalid database type: " + database.get("type").getAsString());
//            GUIShop.LOGGER.warn("Defaulting to SQLite");
//            return this.deserializeAsSqlite(database);
//        }

    }

    @Override
    public JsonElement serialize(DatabaseConfig dbConfig, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        result.addProperty("type", dbConfig.type.name);
        result.addProperty("currency", dbConfig.fileLocation);

        return result;
    }

    private DatabaseConfig deserializeAsSqlite(JsonObject database) {
        String fileLocation = database.has("fileLocation")
                ? database.get("fileLocation").getAsString()
                : "./config/guishop.sqlite";

        return new DatabaseConfig(DatabaseType.SQLITE,fileLocation);
    }
}
