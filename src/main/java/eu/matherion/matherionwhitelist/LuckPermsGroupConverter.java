package eu.matherion.matherionwhitelist;

import com.google.common.reflect.TypeToken;
import cz.maku.mommons.Mommons;
import cz.maku.mommons.ef.converter.TypeConverter;
import net.luckperms.api.model.group.Group;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LuckPermsGroupConverter implements TypeConverter<List<Group>, String> {
    @Override
    public String convertToColumn(List<Group> groups) {
        return Mommons.GSON.toJson(groups.stream()
                .map(Group::getName)
                .toList()
        );
    }

    @Override
    public List<Group> convertToEntityField(String json) {
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> groupNames = Mommons.GSON.fromJson(json, type);
        return new ArrayList<>(groupNames.stream()
                .map(name -> MatherionWhitelist.luckPermsProvider.getGroupManager().getGroup(name))
                .toList());
    }
}
