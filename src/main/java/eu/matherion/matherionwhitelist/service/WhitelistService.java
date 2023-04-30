package eu.matherion.matherionwhitelist.service;

import com.google.common.collect.Lists;
import cz.maku.mommons.ef.Repositories;
import cz.maku.mommons.ef.Tables;
import cz.maku.mommons.ef.repository.Repository;
import cz.maku.mommons.server.Server;
import cz.maku.mommons.storage.database.type.MySQL;
import cz.maku.mommons.worker.annotation.Initialize;
import cz.maku.mommons.worker.annotation.Service;
import eu.matherion.matherionwhitelist.entity.WhitelistedServer;
import net.luckperms.api.model.group.Group;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Service
public class WhitelistService {

    private Repository<String, WhitelistedServer> repository;
    private WhitelistedServer localServer;

    @Initialize
    public void init() {
        try {
            Connection connection = MySQL.getApi().getConnection();
            Tables.createSqlTable(connection, WhitelistedServer.class);
            repository = Repositories.createRepository(connection, WhitelistedServer.class);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | SQLException | NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
    }

    public void initLocal() throws IllegalAccessException {
        WhitelistedServer whitelistedServer = repository.select(Server.local().getId());
        if (whitelistedServer == null) {
            whitelistedServer = new WhitelistedServer();
            whitelistedServer.setName(Server.local().getId());
            whitelistedServer.setGroups(Lists.newArrayList());
            whitelistedServer.setNicknames(Lists.newArrayList());
            whitelistedServer.setEnabled(false);
            repository.createOrUpdate(whitelistedServer);
        }
        localServer = whitelistedServer;
    }

    public boolean whitelist(String nickname) {
        if (!localServer.getNicknames().contains(nickname)) {
            localServer.getNicknames().add(nickname);
            return true;
        }
        localServer.getNicknames().remove(nickname);
        return false;
    }

    public boolean whitelist(Group group) {
        if (!localServer.getGroups().contains(group)) {
            localServer.getGroups().add(group);
            return true;
        }
        localServer.getGroups().remove(group);
        return false;
    }

    public void setEnabled(boolean whitelist) {
        localServer.setEnabled(whitelist);
    }

    public boolean isEnabled() {
        return localServer.isEnabled();
    }

    public boolean isWhitelisted(String nickname) {
        return localServer.getNicknames().contains(nickname);
    }

    public boolean isWhitelisted(Group group) {
        return localServer.getGroups().contains(group);
    }

    public void updateLocal() throws IllegalAccessException {
        repository.createOrUpdate(localServer);
    }

    public CompletableFuture<Boolean> updateLocalAsync() {
        return CompletableFuture.supplyAsync(()-> {
            try {
                updateLocal();
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
