package com.nextdevv.auctions;

import com.google.gson.Gson;
import com.jonahseguin.drink.CommandService;
import com.jonahseguin.drink.Drink;
import com.nextdevv.auctions.commands.AuctionCommand;
import com.nextdevv.auctions.commands.BidCommand;
import com.nextdevv.auctions.config.ConfigurationManager;
import com.nextdevv.auctions.db.DataSource;
import com.nextdevv.auctions.managers.AuctionManager;
import com.nextdevv.auctions.redis.RedisImpl;
import com.nextdevv.kgui.api.KGui;
import io.lettuce.core.RedisClient;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Auctions extends JavaPlugin {

    private ConfigurationManager configurationManager;
    private AuctionManager auctionManager;
    private final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
    private RedisImpl redisManager;
    private Economy economy;
    private KGui kGui;
    private DataSource dataSource;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("======== Auctions ========");
        auctionManager = new AuctionManager();
        auctionManager.updater();

        getLogger().info("Loading config...");
        configurationManager = new ConfigurationManager(this);
        configurationManager.loadConfigs();

        getLogger().info("Loading redis...");
        RedisClient redisClient = RedisClient.create(configurationManager.getSettings().redisUri);
        redisManager = new RedisImpl(redisClient, 4, this);

        getLogger().info("Loading vault...");
        if(!setupEconomy()) {
            getLogger().severe("Vault is not enabled! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Loading commands..."); 
        CommandService drink = Drink.get(this);
        drink.register(new AuctionCommand(), "auction", "auc", "ah", "auction");
        drink.register(new BidCommand(), "bid", "b");
        drink.registerCommands();

        getLogger().info("Loading gui...");
        kGui = new KGui(this);
        kGui.init();

        getLogger().info("Loading database...");
        dataSource = new DataSource(this);
        dataSource.init();

        getLogger().info("======== Auctions ========");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("======== Auctions ========");
        getLogger().info("Plugin has been disabled!");
        redisManager.close();
        if(dataSource != null)
            dataSource.close();
        getLogger().info("======== Auctions ========");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

}
