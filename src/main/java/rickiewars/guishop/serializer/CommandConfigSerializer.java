package rickiewars.guishop.serializer;

import com.google.gson.*;
import rickiewars.guishop.config.EconomyConfig.CommandConfig;

import java.lang.reflect.Type;

public class CommandConfigSerializer implements JsonSerializer<CommandConfig>, JsonDeserializer<CommandConfig> {
    @Override
    public CommandConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject commandsConfigObject = jsonElement.getAsJsonObject();
        CommandConfig commandsConfig = new CommandConfig();

        commandsConfig.disabled = commandsConfigObject.has("disabled")
            && commandsConfigObject.get("disabled").getAsBoolean();
        commandsConfig.alias = commandsConfigObject.has("alias")
            ? commandsConfigObject.get("alias").getAsString() : "";

        return commandsConfig;
    }

    @Override
    public JsonElement serialize(CommandConfig commandsConfig, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        result.addProperty("disabled", commandsConfig.disabled);
        result.addProperty("alias", commandsConfig.alias);

        return result;
    }
}
