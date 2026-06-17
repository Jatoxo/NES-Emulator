package net.jatoxo.json;

import com.google.gson.*;
import net.jatoxo.CPUTest.Cycle;

import java.lang.reflect.Type;


public class CycleTypeAdapter implements JsonDeserializer<Cycle> {

    @Override
    public Cycle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray cycleJson = json.getAsJsonArray();

        int address = cycleJson.get(0).getAsInt();
        byte value  = cycleJson.get(1).getAsByte();
        boolean read = cycleJson.get(2).getAsString().equals("read");

        return new Cycle(address, value, read);
    }


}
