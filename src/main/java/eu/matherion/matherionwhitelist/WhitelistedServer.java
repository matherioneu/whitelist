package eu.matherion.matherionwhitelist;

import cz.maku.mommons.ef.annotation.AttributeConvert;
import cz.maku.mommons.ef.annotation.AttributeName;
import cz.maku.mommons.ef.annotation.Entity;
import cz.maku.mommons.ef.annotation.Id;
import lombok.Getter;
import lombok.Setter;
import net.luckperms.api.model.group.Group;

import java.util.List;

@Entity(name = "whitelisted_servers")
@Getter
@Setter
public class WhitelistedServer {

    @Id
    private String name;
    private boolean enabled;
    @AttributeName("luckperms_groups")
    @AttributeConvert(converter = LuckPermsGroupConverter.class)
    private List<Group> groups;
    @AttributeConvert(converter = NicknamesConverter.class)
    private List<String> nicknames;
}
