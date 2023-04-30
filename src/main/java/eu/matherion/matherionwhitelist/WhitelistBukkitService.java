package eu.matherion.matherionwhitelist;

import cz.maku.mommons.worker.annotation.BukkitCommand;
import cz.maku.mommons.worker.annotation.BukkitEvent;
import cz.maku.mommons.worker.annotation.Load;
import cz.maku.mommons.worker.annotation.Service;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

@Service(commands = true, listener = true)
public class WhitelistBukkitService {

    @Load
    private WhitelistService whitelistService;

    @BukkitCommand(value = "whitelist", aliases = {"wl", "mwhitelist", "mwl"})
    public void whitelistCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("matherion.whitelist")) {
                player.sendMessage("§cNemáš dostatečná oprávnění.");
                return;
            }
        }
        if (args.length == 0) {
            sender.sendMessage("§c/whitelist on/off");
            sender.sendMessage("§c/whitelist group <group>");
            sender.sendMessage("§c/whitelist user <user>");
            sender.sendMessage("§c/whitelist sync");
            return;
        }
        switch (args[0]) {
            case "on" -> {
                whitelistService.setEnabled(true);
                whitelistService.updateLocalAsync().thenAccept(success -> {
                    if (success) {
                        sender.sendMessage("§aWhitelist zapnut.");
                        return;
                    }
                    sender.sendMessage("§cNastala chyba při ukládání whitelistu.");
                });
            }
            case "off" -> {
                whitelistService.setEnabled(false);
                whitelistService.updateLocalAsync().thenAccept(success -> {
                    if (success) {
                        sender.sendMessage("§aWhitelist vypnut.");
                        return;
                    }
                    sender.sendMessage("§cNastala chyba při ukládání whitelistu.");
                });
            }
            case "group" -> {
                if (args.length == 1) {
                    sender.sendMessage("§c/whitelist group <group>");
                    return;
                }
                LuckPerms luckPermsProvider = MatherionWhitelist.luckPermsProvider;
                Group group = luckPermsProvider.getGroupManager().getGroup(args[1]);
                if (group == null) {
                    sender.sendMessage("§cSkupina " + args[1] + " neexistuje.");
                    return;
                }
                boolean added = whitelistService.whitelist(group);
                whitelistService.updateLocalAsync().thenAccept(success -> {
                    if (success) {
                        if (added) {
                            sender.sendMessage("§aSkupina " + args[1] + " byla přidána na whitelist.");
                            return;
                        }
                        sender.sendMessage("§aSkupina " + args[1] + " byla odebrána z whitelistu.");
                        return;
                    }
                    sender.sendMessage("§cNastala chyba při ukládání whitelistu.");
                });
            }
            case "user" -> {
                if (args.length == 1) {
                    sender.sendMessage("§c/whitelist user <user>");
                    return;
                }
                boolean added = whitelistService.whitelist(args[1]);
                whitelistService.updateLocalAsync().thenAccept(success -> {
                    if (success) {
                        if (added) {
                            sender.sendMessage("§aHráč " + args[1] + " byl přidán na whitelist.");
                            return;
                        }
                        sender.sendMessage("§aHráč " + args[1] + " byl odebrán z whitelistu.");
                        return;
                    }
                    sender.sendMessage("§cNastala chyba při ukládání whitelistu.");
                });
            }
            case "sync" -> {
                try {
                    whitelistService.initLocal();
                    sender.sendMessage("§aWhitelist byl úspěšně znovu načten.");
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                    sender.sendMessage("§cNastala chyba při načítání whitelistu.");
                }
            }
            default -> {
                sender.sendMessage("§c/whitelist on/off");
                sender.sendMessage("§c/whitelist group <group>");
                sender.sendMessage("§c/whitelist user <user>");
                sender.sendMessage("§c/whitelist sync");
            }
        }
    }

    @BukkitEvent(value = AsyncPlayerPreLoginEvent.class, priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!whitelistService.isEnabled()) return;
        String playerName = event.getName();
        if (whitelistService.isWhitelisted(playerName)) return;
        LuckPerms luckPermsProvider = MatherionWhitelist.luckPermsProvider;
        User user = luckPermsProvider.getUserManager().getUser(playerName);
        if (user == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "§cNastala chyba při načítání uživatele.");
            return;
        }
        Group group = luckPermsProvider.getGroupManager().getGroup(user.getPrimaryGroup());
        if (group == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "§cNastala chyba při načítání skupiny.");
            return;
        }
        if (whitelistService.isWhitelisted(group)) return;
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "§cNejsi na whitelistu.");
    }
}
