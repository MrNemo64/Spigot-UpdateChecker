package de.jeff_media.updatechecker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

class Messages {

		@NotNull
    private static TextComponent createLink(@NotNull final String text, @NotNull final String link) {
        final ComponentBuilder lore = new ComponentBuilder("Link: ")
                .bold(true)
                .append(link)
                .bold(false);
        final TextComponent component = new TextComponent(text);
        component.setBold(true);
        // TODO: Make color configurable
        component.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        //noinspection deprecation
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, lore.create()));
        return component;
    }

    protected static void printCheckResultToConsole(UpdateCheckEvent event) {

        final UpdateChecker instance = UpdateChecker.getInstance();
        final Plugin plugin = instance.getPlugin();

        if (event.getSuccess() == UpdateCheckSuccess.FAIL || event.getResult() == UpdateCheckResult.UNKNOWN) {
            plugin.getLogger().warning("Could not check for updates.");
            return;
        }

        if (event.getResult() == UpdateCheckResult.RUNNING_LATEST_VERSION) {
            plugin.getLogger().info(String.format("You are using the latest version of %s.", plugin.getName()));
            return;
        }

        List<String> lines = new ArrayList<>();

        lines.add(String.format("There is a new version of %s available!", plugin.getName()));
        lines.add(String.format("  Your version: %s%s", instance.isColoredConsoleOutput() ? ChatColor.RED : "", event.getUsedVersion()));
        lines.add(String.format("Latest version: %s%s", instance.isColoredConsoleOutput() ? ChatColor.GREEN : "", event.getLatestVersion()));

        List<String> downloadLinks = instance.getAppropiateDownloadLinks();

        if (downloadLinks.size() > 0) {
            lines.add(" ");
            lines.add("Please update to the newest version.");
            lines.add(" ");
            if (downloadLinks.size() == 1) {
                lines.add("Download:");
                lines.add("  " + downloadLinks.get(0));
            } else if (downloadLinks.size() == 2) {
                lines.add("Download (Plus):");
                lines.add("  " + downloadLinks.get(0));
                lines.add(" ");
                lines.add("Download (Free):");
                lines.add("  " + downloadLinks.get(1));
            }
        }

        printNiceBoxToConsole(plugin.getLogger(), Level.WARNING, lines, 120, "*", true);
    }

    protected static void printCheckResultToPlayer(Player player, boolean showMessageWhenLatestVersion) {
        UpdateChecker instance = UpdateChecker.getInstance();
        if(instance.getLastCheckResult() == UpdateCheckResult.NEW_VERSION_AVAILABLE) {
            player.sendMessage(ChatColor.GRAY + "There is a new version of " + ChatColor.GOLD + instance.getPlugin().getName() + ChatColor.GRAY + " available.");
            sendLinks(player);
            player.sendMessage(ChatColor.DARK_GRAY + "Latest version: " + ChatColor.GREEN + instance.getCachedLatestVersion() + ChatColor.DARK_GRAY + " | Your version: " + ChatColor.RED + instance.getUsedVersion());
            player.sendMessage("");
        } else if(instance.getLastCheckResult() == UpdateCheckResult.UNKNOWN) {
            player.sendMessage(ChatColor.GOLD + instance.getPlugin().getName() + ChatColor.RED + " could not check for updates.");
        } else {
            if(showMessageWhenLatestVersion) {
                player.sendMessage(ChatColor.GREEN+"You are running the latest version of " + ChatColor.GOLD + instance.getPlugin().getName());
            }
        }
    }

    private static void printNiceBoxToConsole(Logger logger, Level level, List<String> lines, int maxLineLengh, String dashSymbol, boolean prefix) {
        int longestLine = 0;
        for (String line : lines) {
            longestLine = Math.max(line.length(), longestLine);
        }
        longestLine += 2;
        if (longestLine > maxLineLengh) longestLine = maxLineLengh;
        if (prefix) longestLine += 2;
        StringBuilder dash = new StringBuilder(longestLine);
        Stream.generate(() -> dashSymbol).limit(longestLine).forEach(dash::append);

        logger.log(level, dash.toString());
        for (String line : lines) {
            logger.log(level, (prefix ? dashSymbol + " " : "") + line);
        }
        logger.log(level, dash.toString());
    }

    private static void sendLinks(@NotNull final Player... players) {

        UpdateChecker instance = UpdateChecker.getInstance();

        List<TextComponent> links = new ArrayList<>();

        List<String> downloadLinks = instance.getAppropiateDownloadLinks();

        if (downloadLinks.size() == 2) {
            links.add(createLink(String.format("Download (%s)", instance.getNamePaidVersion()), downloadLinks.get(0)));
            links.add(createLink(String.format("Download (%s)", instance.getNameFreeVersion()), downloadLinks.get(1)));
        } else if (downloadLinks.size() == 1) {
            links.add(createLink("Download", downloadLinks.get(0)));
        }
        if (instance.getDonationLink() != null) {
            links.add(createLink("Donate", instance.getDonationLink()));
        }
        if (instance.getChangelogLink() != null) {
            links.add(createLink("Changelog", instance.getChangelogLink()));
        }

        final TextComponent placeholder = new TextComponent(" | ");
        placeholder.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        TextComponent text = new TextComponent("");

        Iterator<TextComponent> iterator = links.iterator();
        while (iterator.hasNext()) {
            TextComponent next = iterator.next();
            text.addExtra(next);
            if (iterator.hasNext()) {
                text.addExtra(placeholder);
            }
        }

        // MrNemo64 start
        for(Player player : players)
        	player.spigot().sendMessage(text);
       // MrNemo64 end
    }
}
