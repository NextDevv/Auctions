package com.nextdevv.auctions.db;

import com.nextdevv.auctions.Auctions;
import com.nextdevv.auctions.commands.AuctionOwner;
import com.nextdevv.auctions.models.AuctionLog;
import com.nextdevv.auctions.models.Bid;
import com.nextdevv.auctions.models.Bidder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.callback.Callback;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("SqlSourceToSinkFlow")
public class DataSource {
    private final ExecutorService executor;
    private final HikariDataSource ds;
    private final Auctions plugin;

    public DataSource(@NotNull Auctions plugin) {
        this.plugin = plugin;
        executor = Executors.newFixedThreadPool(10);

        String host = plugin.getConfigurationManager().getSettings().database.host();
        int port = plugin.getConfigurationManager().getSettings().database.port();
        HikariConfig config = getHikariConfig(plugin, host, port);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    @NotNull
    private static HikariConfig getHikariConfig(@NotNull Auctions plugin, String host, int port) {
        String database = plugin.getConfigurationManager().getSettings().database.name();
        String username = plugin.getConfigurationManager().getSettings().database.username();
        String password = plugin.getConfigurationManager().getSettings().database.password();
        String additional = plugin.getConfigurationManager().getSettings().database.additional();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?" + additional);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(10);
        config.setPoolName("ClaimBank");
        config.setMaxLifetime(120000);
        config.setKeepaliveTime(0);
        config.setConnectionTimeout(5000);
        return config;
    }

    public void close() {
        executor.shutdownNow();
        ds.close();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void init() {
        // Create a log table with these parameters
        // id (primary key)
        //uuid di chi ha startato l'asta (auc_uuid)
        //nickname di chi ha startato (auc_nick)
        //uuid di chi ha vinto l'asta (auc_winner_uuid)
        //nickname di chi ha vinto l'asta (auc_winner_nick)
        //date
        //oggetto (base64)
        //numero di offerte (offers)

        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                connection.prepareStatement("CREATE TABLE IF NOT EXISTS auctions_logs_3 (" +
                        "id INT NOT NULL AUTO_INCREMENT," +
                        "auc_uuid VARCHAR(36) NOT NULL," +
                        "auc_nick VARCHAR(16) NOT NULL," +
                        "auc_winner_uuid VARCHAR(36) NOT NULL," +
                        "auc_winner_nick VARCHAR(16) NOT NULL," +
                        "auc_date VARCHAR(36) NOT NULL," +
                        "auc_item TEXT NOT NULL," +
                        "auc_bid DOUBLE NOT NULL," +
                        "auc_offers INT NOT NULL," +
                        "PRIMARY KEY (id))").executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor).whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                plugin.getLogger().severe("Error while creating table auctions");
                throwable.printStackTrace();
            }
        });
    }

    public void insertAuctionLog(AuctionLog auctionLog) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                connection.prepareStatement("INSERT INTO auctions_logs_3 (auc_uuid, auc_nick, auc_winner_uuid, auc_winner_nick, auc_date, auc_item, auc_bid, auc_offers) VALUES ('" +
                        auctionLog.auctionOwner().getUuid() + "', '" +
                        auctionLog.auctionOwner().getName() + "', '" +
                        auctionLog.winner().getBidder().getUuid() + "', '" +
                        auctionLog.winner().getBidder().getName() + "', '" +
                        auctionLog.formattedDate() + "', '" +
                        auctionLog.serializedItem() + "', " +
                        auctionLog.winner().getAmount() + ", " +
                        auctionLog.totalBids() + ")").executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor).whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                plugin.getLogger().severe("Error while inserting auction log");
                throwable.printStackTrace();
            }
        });
    }

    public CompletableFuture<List<AuctionLog>> getOwnerAuctions(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                ResultSet resultSet = connection.prepareStatement("SELECT * FROM auctions_logs_3 WHERE auc_uuid = '" + uuid.toString() + "'").executeQuery();
                List<AuctionLog> auctionLogs = new ArrayList<>();
                while (resultSet.next()) {
                    auctionLogs.add(new AuctionLog(
                            new AuctionOwner(resultSet.getString("auc_uuid"), resultSet.getString("auc_nick")),
                            new Bid(new Bidder(resultSet.getString("auc_winner_uuid"), resultSet.getString("auc_winner_nick")), resultSet.getDouble("auc_bid")),
                            resultSet.getString("auc_date"),
                            resultSet.getString("auc_item"),
                            resultSet.getInt("auc_offers")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<List<AuctionLog>> getWinnerAuctions(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                ResultSet resultSet = connection.prepareStatement("SELECT * FROM auctions_logs_3 WHERE auc_winner_uuid = '" + uuid.toString() + "'").executeQuery();
                List<AuctionLog> auctionLogs = new ArrayList<>();
                while (resultSet.next()) {
                    auctionLogs.add(new AuctionLog(
                            new AuctionOwner(resultSet.getString("auc_uuid"), resultSet.getString("auc_nick")),
                            new Bid(new Bidder(resultSet.getString("auc_winner_uuid"), resultSet.getString("auc_winner_nick")), resultSet.getDouble("auc_bid")),
                            resultSet.getString("auc_date"),
                            resultSet.getString("auc_item"),
                            resultSet.getInt("auc_offers")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<List<AuctionLog>> getAuctions() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                ResultSet resultSet = connection.prepareStatement("SELECT * FROM auctions_logs_3").executeQuery();
                List<AuctionLog> auctionLogs = new ArrayList<>();
                while (resultSet.next()) {
                    auctionLogs.add(new AuctionLog(
                            new AuctionOwner(resultSet.getString("auc_uuid"), resultSet.getString("auc_nick")),
                            new Bid(new Bidder(resultSet.getString("auc_winner_uuid"), resultSet.getString("auc_winner_nick")), resultSet.getDouble("auc_bid")),
                            resultSet.getString("auc_date"),
                            resultSet.getString("auc_item"),
                            resultSet.getInt("auc_offers")
                    ));
                }
                return auctionLogs;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }
}
