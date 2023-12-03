package com.nextdevv.auctions.config;

import com.nextdevv.auctions.Auctions;
import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import lombok.Getter;

import java.io.File;

public class ConfigurationManager {
    private final Auctions plugin;
    @Getter
    private Settings settings;
    @Getter
    private Messages messages;


    public ConfigurationManager(Auctions plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    public void loadConfigs() {
        YamlConfigurationProperties properties = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
                .footer("Authors: Nexxt, AlexDev_")
                .build();
        File settingsFile = new File(plugin.getDataFolder(), "config.yml");

        settings = YamlConfigurations.update(
                settingsFile.toPath(),
                Settings.class,
                properties
        );

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        messages = YamlConfigurations.update(
                messagesFile.toPath(),
                Messages.class,
                properties
        );
    }

    public void saveConfigs() {
        YamlConfigurationProperties properties = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
                .footer("Authors: Nexxt, AlexDev_")
                .build();
        File settingsFile = new File(plugin.getDataFolder(), "config.yml");
        YamlConfigurations.save(
                settingsFile.toPath(),
                Settings.class,
                settings,
                properties
        );
        YamlConfigurations.save(new File(plugin.getDataFolder(), "messages.yml").toPath(), Messages.class, messages);
    }

    public void reload() {
        YamlConfigurationProperties properties = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
                .footer("Authors: Nexxt, AlexDev_")
                .build();
        File settingsFile = new File(plugin.getDataFolder(), "config.yml");

        settings = YamlConfigurations.load(
                settingsFile.toPath(),
                Settings.class,
                properties
        );

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        messages = YamlConfigurations.load(
                messagesFile.toPath(),
                Messages.class,
                properties
        );
    }
}
