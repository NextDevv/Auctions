package com.nextdevv.auctions.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Broadcast {
    public static void send(String message) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
