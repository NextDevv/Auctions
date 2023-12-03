package com.nextdevv.auctions.config;

import de.exlll.configlib.Configuration;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class Messages {
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public String onlyPlayers = "&cQuesto comando può essere eseguito solo da un giocatore.";
    public String vanishSettingsSpectator = "&cVillag3r_ ha detto che non puoi usare questo comando in modalità spettatore. :(";

    public String color(String text) {
        return getString(text, hexPattern);
    }

    @NotNull
    public static String getString(String text, Pattern hexPattern) {
        if (text == null) return "";

        text = ChatColor.translateAlternateColorCodes('&', text);

        final char colorChar = ChatColor.COLOR_CHAR;

        final Matcher matcher = hexPattern.matcher(text);
        final StringBuffer buffer = new StringBuffer(text.length() + 4 * 8);

        while (matcher.find()) {
            final String group = matcher.group(1);

            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }

        text = matcher.appendTail(buffer).toString();

        return text;
    }

    // messages
    public String help =
            "&7&m----------------------------------------\n" +
            "&e&lAuctions\n" +
            "&7&m----------------------------------------\n" +
            "&e/auction start <price> <duration> <item> &7- &fAvvia un'asta\n" +
            "&e/auction stop &7- &fFerma l'asta in corso\n" +
            "&e/auction info &7- &fMostra le informazioni dell'asta in corso\n" +
            "&7&m----------------------------------------\n";

    public String auctionStarted = "&aAsta iniziata da &e%player%&a per &e%item%&a!";
    public String auctionEnded = "&aAsta terminata!";
    public String timeLeft = "&aRimangono &e%time%&a secondi alla fine dell'asta.";
    public String mustBePlayer = "&cQuesto comando può essere eseguito solo da un giocatore.";
    public String noAuction = "&cNon c'è nessuna asta in corso.";
    public String auctionAddedToQueue = "&aAsta aggiunta alla coda!";
    public String notYourAuction = "&cQuesta asta non è tua.";
    public String cantBidOnOwnAuction = "&cNon puoi fare offerte sulla tua asta.";
    public String notEnoughMoney = "&cNon hai abbastanza soldi.";
    public String bidPlaced = "&aOfferta effettuata!";
    public String mustHoldItem = "&cDevi tenere un oggetto in mano.";
    public String endedWithoutBids = "&aL'asta è terminata senza offerte.";
    public String bidPlacedBySomeone = "&a%player% offre &e%amount%&a!";
    public String cantCancelBid = "&cNon puoi cancellare l'offerta.";
    public String bidCancelled = "&aOfferta cancellata.";
    public String cantCancelAuction = "&cNon puoi cancellare l'asta.";
    public String noBid = "&cNon hai fatto nessuna offerta.";
    public String auctionCancelled = "&aAsta cancellata.";
    public String alreadyCreated = "&cHai già creato un'asta.";
    public String bidLessThanStartingPrice = "&cL'offerta deve essere maggiore del prezzo di partenza.";
    public String itemNotFoundInInventory = "&cNon hai questo oggetto nell'inventario.";
    public String preconditionsFailed = "&cNon puoi avviare l'asta.";

    public String maxAuctionTime = "&cNon puoi avviare un'asta con una durata maggiore di %time% secondi.";

    public String minAuctionTime = "&cNon puoi avviare un'asta con una durata minore di %time% secondi.";

    public String clickToViewItem = "&aClicca per vedere l'oggetto.";
    public String playerNotFound = "&cGiocatore non trovato.";
    public String logsEmpty = "&cNon ci sono aste registrate.";
    public String invalidInput = "&cInput non valido.";
    public String noLogsFound = "&cNessun log trovato.";

    public String lastSecondsMessage = "&aL'asta terminerà tra %time% secondi.";

    @SneakyThrows
    public void sendMessage(CommandSender sender, String message, String... placeholders) {
        Field field = this.getClass().getDeclaredField(message);
        String msg = (String) field.get(this);
        if(placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        sender.sendMessage(color(msg));
    }

    public void send(CommandSender commandSender, String text) {
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }
}
