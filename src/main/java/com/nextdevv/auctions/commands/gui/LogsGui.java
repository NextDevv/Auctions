package com.nextdevv.auctions.commands.gui;

import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.config.Messages;
import com.nextdevv.auctions.db.DataSource;
import com.nextdevv.auctions.enums.SearchFilter;
import com.nextdevv.auctions.models.AuctionLog;
import com.nextdevv.auctions.utils.ClassSerializer;
import com.nextdevv.auctions.utils.Presets;
import com.nextdevv.kgui.api.KGui;
import com.nextdevv.kgui.item.KItemStack;
import com.nextdevv.kgui.models.GuiBorder;
import com.nextdevv.kgui.models.GuiButton;
import com.nextdevv.kgui.models.Pages;
import com.nextdevv.kgui.utils.Alignment;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LogsGui {
    private final Auctions PLUGIN = Auctions.getPlugin(Auctions.class);

    public void send(String message, CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private <T> List<T> reverse(List<T> list) {
        List<T> reversed = new ArrayList<>();
        for(int i = list.size() - 1; i >= 0; i--) {
            reversed.add(list.get(i));
        }
        return reversed;
    }

    // TODO: Fix this
    public static Map<ItemStack, AuctionLog> sortAuctionMapByDate(Map<ItemStack, AuctionLog> auctionMap) {
        List<Map.Entry<ItemStack, AuctionLog>> entryList = new LinkedList<>(auctionMap.entrySet());

        entryList.sort((entry1, entry2) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yy HH:mm:ss");
            try {
                Date date1 = dateFormat.parse(entry1.getValue().formattedDate());
                Date date2 = dateFormat.parse(entry2.getValue().formattedDate());
                return date2.compareTo(date1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        return entryList.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    public void open(CommandSender sender, SearchFilter filter, String filterName) {
        Messages messages = PLUGIN.getConfigurationManager().getMessages();
        DataSource dataSource = PLUGIN.getDataSource();

        dataSource.getAuctions().thenAccept(auctions -> {
            if (auctions == null) {
                return;
            }

            Player player = (Player) sender;
            KGui kGui = PLUGIN.getKGui();

            GuiBorder border = new GuiBorder();
            ItemStack borderItem = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
            ItemMeta meta = borderItem.getItemMeta();
            meta.setDisplayName(" ");
            borderItem.setItemMeta(meta);
            border.setDefaultItemStack(borderItem);

            HashMap<ItemStack, AuctionLog> logs = new HashMap<>();

            switch (filter) {
                case AUCTION_OWNER, AUCTION_WINNER, ALL -> {
                    if (filterName == null) {
                        send(messages.invalidInput, sender);
                        return;
                    }

                    auctions.stream()
                            .filter(auction -> {
                                String lowerCaseFilterName = filterName.toLowerCase();

                                switch (filter) {
                                    case AUCTION_OWNER -> { return auction.auctionOwner().getName().toLowerCase().contains(lowerCaseFilterName); }
                                    case AUCTION_WINNER -> { return auction.winner().getBidder().getName().toLowerCase().contains(lowerCaseFilterName); }
                                    default -> { return true; }
                                }
                            })
                            .forEach(auction -> {
                                ItemStack deserializedItem = ClassSerializer.FromStringItemStack(auction.serializedItem());
                                Material mat = deserializedItem.getType();
                                boolean hasEnchants = !deserializedItem.getEnchantments().isEmpty();
                                KItemStack.Builder item = new KItemStack().builder()
                                        .setMaterial(mat)
                                        .setName("&e" + auction.auctionOwner().getName() + " &7- &e" + auction.formattedDate());

                                if(hasEnchants) {
                                    item.addEnchantment(Enchantment.ARROW_FIRE);
                                    item.addItemFlag(ItemFlag.HIDE_ENCHANTS);
                                }

                                logs.put(item.build(), auction);
                            });
                }
            }

            if (logs.isEmpty()) {
                send(messages.noLogsFound, sender);
                return;
            }

            // TODO: Needs a rework
            // logs = (HashMap<ItemStack, AuctionLog>) sortAuctionMapByDate(logs);

            // for now try and reverse the list
            Pages pages = new Pages().builder().setMaxPerPages(28).autoCreatePages(reverse(logs.keySet().stream().toList())).build();

            Inventory gui = kGui.builder(player)
                    .setTitle(ChatColor.translateAlternateColorCodes('&', "&eLogs"))
                    .setRows(6)
                    .setBorder(border)
                    .setPages(pages)
                    .addButton(Alignment.BOTTOM_LEFT, new GuiButton().setItemStack(
                            new KItemStack().builder()
                                    .setMaterial(Material.ARROW)
                                    .setName("&ePrevious page")
                                    .build()
                    ).onClick((builder, player1) -> {
                        int currentPage = builder.getCurrentPage();
                        if (currentPage == 1) return null;
                        Inventory newGui = builder.setCurrentPage2(currentPage - 1).build();
                        player1.openInventory(newGui);
                        return null;
                    }))
                    .addButton(Alignment.BOTTOM_RIGHT, new GuiButton().setItemStack(
                            new KItemStack().builder()
                                    .setMaterial(Material.ARROW)
                                    .setName("&eNext page")
                                    .build()
                    ).onClick((builder, player1) -> {
                        int currentPage = builder.getCurrentPage();
                        if (currentPage == pages.getPages().size()) return null;
                        Inventory newGui = builder.setCurrentPage2(currentPage + 1).build();
                        player1.openInventory(newGui);
                        return null;
                    }))
                    .addButton(Alignment.TOP_LEFT, new GuiButton().setItemStack(
                            new KItemStack().builder()
                                    .setMaterial(Material.COMPASS)
                                    .setName("&eSearch")
                                    .build()
                    ).onClick(((builder, player1) -> {
                        Inventory searchSelection = kGui.builder(player1)
                                .setBorder(border)
                                .setRows(3)
                                .setTitle(ChatColor.translateAlternateColorCodes('&', "&eSearch"))
                                .addButton(Alignment.TOP_LEFT, new GuiButton().setItemStack(
                                        new KItemStack().builder()
                                                .setMaterial(Material.ARROW)
                                                .setName("&eBack")
                                                .build()
                                ).onClick((builder1, player2) -> {
                                    player2.openInventory(builder.build());
                                    return null;
                                }))
                                .addButton(12, new GuiButton().setItemStack(new KItemStack().builder()
                                        .setMaterial(Material.EMERALD_BLOCK)
                                        .setName("&aSearch by Auction Owner")
                                        .build()
                                ).onClick((builder1, player2) -> {
                                    builder1.askForInput(player2, "&aEnter in chat the auction owner name:").thenAccept(res -> {
                                        player2.sendMessage(res);
                                        if (res.isEmpty()) {
                                            player2.closeInventory();
                                            player2.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid input"));
                                            return;
                                        }

                                        List<AuctionLog> filteredLogs = auctions.stream()
                                                .filter(log -> log.auctionOwner().getName().toLowerCase().contains(res)).toList();
                                        if (filteredLogs.isEmpty()) {
                                            player2.closeInventory();
                                            player2.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo logs found"));
                                            return;
                                        }

                                        open(player2, SearchFilter.AUCTION_OWNER, res);
                                    });
                                    return null;
                                }))
                                .addButton(14, new GuiButton().setItemStack(new KItemStack().builder()
                                        .setMaterial(Material.EMERALD_BLOCK)
                                        .setName("&aSearch by Auction Winner")
                                        .build()
                                ).onClick((builder1, player2) -> {
                                    builder1.askForInput(player2, "&aEnter in chat the auction winner name:").thenAccept(res -> {
                                        if (res == null || res.isEmpty()) {
                                            player2.closeInventory();
                                            player2.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid input"));
                                            return;
                                        }

                                        List<AuctionLog> filteredLogs = auctions.stream()
                                                .filter(log -> log.winner().getBidder().getName().toLowerCase().contains(res)).toList();
                                        if (filteredLogs.isEmpty()) {
                                            player2.closeInventory();
                                            player2.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo logs found"));
                                            return;
                                        }

                                        open(player2, SearchFilter.AUCTION_WINNER, res);
                                    });
                                    return null;
                                }))
                                .build();
                        player1.openInventory(searchSelection);
                        return null;
                    })))
                    .addItemStackClickListener(((itemStack, player1, builder, clickType) -> {
                        AuctionLog log = logs.get(itemStack);

                        ItemStack playerHead = createHeadItem(log.auctionOwner().getName(), "&fAuction Owner:&e "+ ChatColor.translateAlternateColorCodes('&', "&e" + log.auctionOwner().getName()));
                        ItemStack bidderHead = createHeadItem(log.winner().getBidder().getName(), "&fAuction Winner:&e "+ ChatColor.translateAlternateColorCodes('&', "&e" + log.winner().getBidder().getName()));
                        ItemStack totalBids = createMetaItem(Presets.SOME_DUDE, "&fTotal Bids:&e ", String.valueOf(log.totalBids()));
                        ItemStack bidAmount = createMetaItem(Presets.GOLDEN_COINS_HEAD, "&fBid Amount:&e ", PLUGIN.getEconomy().format(log.winner().getAmount()));

                        Inventory newGui = kGui.builder(player1)
                                .setTitle(ChatColor.translateAlternateColorCodes('&', "&e" + log.auctionOwner().getName() + " &7- &e" + log.formattedDate()))
                                .setRows(5)
                                .setBorder(border)
                                .addButton(Alignment.TOP_LEFT, new GuiButton().setItemStack(
                                        new KItemStack().builder()
                                                .setMaterial(Material.ARROW)
                                                .setName("&eBack")
                                                .build()
                                ).onClick((builder1, player2) -> {
                                    player2.openInventory(builder.build());
                                    return null;
                                }))
                                .setItem(playerHead, 19)
                                .setItem(bidderHead, 20)
                                .addButton(22, new GuiButton().setItemStack(
                                        ClassSerializer.FromStringItemStack(log.serializedItem())
                                ).setOnClick(((builder1, player2) -> {
                                    player2.getInventory().addItem(ClassSerializer.FromStringItemStack(log.serializedItem()));
                                    return null;
                                })))
                                .setItem(totalBids, 24)
                                .setItem(bidAmount, 25)
                                .build();
                        player1.openInventory(newGui);
                        return null;
                    }))
                    .build();

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.openInventory(gui);
                }
            }.runTask(PLUGIN);
        });
    }

    private ItemStack createHeadItem(String playerName, String displayName) {
        ItemStack headItem = Presets.getPlayerHead(playerName);
        ItemMeta headItemMeta = headItem.getItemMeta();
        headItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        headItem.setItemMeta(headItemMeta);
        return headItem;
    }

    private ItemStack createMetaItem(ItemStack presetItem, String displayName, String metaDisplayName) {
        ItemStack newItem = presetItem.clone();
        ItemMeta newItemMeta = newItem.getItemMeta();
        newItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName + metaDisplayName));
        newItem.setItemMeta(newItemMeta);
        return newItem;
    }
}
