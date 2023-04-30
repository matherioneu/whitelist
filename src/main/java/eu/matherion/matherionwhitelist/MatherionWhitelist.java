package eu.matherion.matherionwhitelist;

import com.google.common.collect.Lists;
import cz.maku.mommons.worker.WorkerReceiver;
import cz.maku.mommons.worker.plugin.WorkerPlugin;
import eu.matherion.matherionwhitelist.service.WhitelistBukkitService;
import eu.matherion.matherionwhitelist.service.WhitelistService;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;

public final class MatherionWhitelist extends WorkerPlugin {

    public static LuckPerms luckPermsProvider;

    @Override
    public List<Class<?>> registerServices() {
        return Lists.newArrayList(WhitelistService.class, WhitelistBukkitService.class);
    }

    @Override
    public void load() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsProvider = provider.getProvider();
        }
        WhitelistService whitelistService = WorkerReceiver.getService(MatherionWhitelist.class, WhitelistService.class);
        if (whitelistService == null) return;
        try {
            whitelistService.initLocal();
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void unload() {

    }
}
