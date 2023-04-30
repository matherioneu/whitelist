package eu.matherion.matherionwhitelist.entity;

import com.google.common.reflect.TypeToken;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.ef.converter.TypeConverter;

import java.lang.reflect.Type;
import java.util.List;

public class NicknamesConverter implements TypeConverter<List<String>, String> {
    @Override
    public String convertToColumn(List<String> groups) {
        return Mommons.GSON.toJson(groups);
    }

    @Override
    public List<String> convertToEntityField(String json) {
        Type type = new TypeToken<List<String>>() {
        }.getType();
        return Mommons.GSON.fromJson(json, type);
    }
}
