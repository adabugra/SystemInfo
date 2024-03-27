/*
 *     SystemInfo - The Master of Server Hardware
 *     Copyright © 2024 CMarco
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.cmarco.systeminfo.commands.systeminfo;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.cmarco.systeminfo.commands.SystemInfoCommand;
import top.cmarco.systeminfo.enums.Messages;
import top.cmarco.systeminfo.plugin.SystemInfo;
import top.cmarco.systeminfo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

import static org.bukkit.World.Environment.*;

/**
 * The `CommandSystemInfo` class is a Spigot command that provides various system-related information and commands
 * for the SystemInfo plugin.
 */
public final class CommandSystemInfo extends SystemInfoCommand {

    /**
     * Initializes a new instance of the `CommandSystemInfo` class.
     *
     * @param systemInfo The `SystemInfo` instance associated with this command.
     */
    public CommandSystemInfo(@NotNull SystemInfo systemInfo) {
        super(systemInfo, "systeminfo",
                "main command of SystemInfo plugin",
                "/<command> [stats|version|reload|gui]",
                Collections.emptyList());
    }

    /**
     * Executes the "/systeminfo" command and its subcommands based on the provided arguments.
     *
     * @param sender The command sender.
     * @param name   The command name.
     * @param args   The command arguments.
     * @return True if the command was executed successfully; otherwise, false.
     */
    @Override
    public boolean execute(CommandSender sender, @NotNull String name, String[] args) {
        if (sender.hasPermission("systeminfo.commands.help")) {
            if (args.length == 0) {
                systemInfo1(sender);
                return true;
            } else if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "2":
                        systemInfo2(sender);
                        break;
                    case "version":
                        sender.sendMessage(Utils.color("&2» &7SystemInfo version: &a" + systemInfo.getDescription().getVersion()));
                        break;
                    case "stats":
                        stats(sender);
                        break;
                    case "reload":
                        reload(sender);
                        break;
                    case "gui":
                        if (sender instanceof Player) {
                            systemInfo.getSystemInfoGui().createGui((Player) sender);
                        } else {
                            sender.sendMessage(Utils.color("&4» &cYou cannot create GUIs inside a console"));
                        }
                        break;
                    default:
                        sender.sendMessage(Messages.INVALID_ARGS.value(true));
                        break;
                }
            }
        }
        return true;
    }

    /**
     * Displays the first page of system information and commands in chat.
     *
     * @param sender The command sender.
     */
    private static void systemInfo1(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sender.sendMessage(Utils.color("&7&m&l--------------------------------------"));
            player.spigot().sendMessage(Utils.builderHover("&7»» &fSystemInfo Help &7««", "This is the help page."));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/lscpu &aget processor info! &8[&7*&8]", "this returns processor info"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/gpu &aget gpu info! &8[&7*&8]", "prints all necessary GPU info."));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/vmstat &aget memory info! &8[&7*&8]", "this gets memory usage\nof the entire host"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/sensors &aget sensors info! &8[&7*&8]", "gets various info from sensors"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/disks &aget disks info! &8[&7*&8]", "prints out a map of disks."));
            player.spigot().sendMessage(Utils.builderClick("&7»» Click here for the second page &8[&7*&8]", "/systeminfo 2"));
            player.sendMessage(Utils.color("&7&m&l--------------------------------------"));
        } else {
            sender.sendMessage(Utils.color("&7&m&l--------------------------------------"));
            sender.sendMessage(Utils.color("&7»» &fSystemInfo Help &7««"));
            sender.sendMessage(Utils.color("&f- &7/lscpu &aget processor info!"));
            sender.sendMessage(Utils.color("&f- &7/gpu &aget gpu info!"));
            sender.sendMessage(Utils.color("&f- &7/vmstat &aget memory info!"));
            sender.sendMessage(Utils.color("&f- &7/sensors &aget sensors info!"));
            sender.sendMessage(Utils.color("&f- &7/disks &aget disks info!"));
            sender.sendMessage(Utils.color("&7&m&l--------------------------------------"));
        }
    }

    /**
     * Displays the second page of system information and commands in chat.
     *
     * @param sender The command sender.
     */
    private static void systemInfo2(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sender.sendMessage(Utils.color("&7&l&m--------------------------------------"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/htop &aget processes list! &8[&7*&8]", "get a list of processes"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/systeminfo [reload&f|&7version&f|&7stats&f|&7gui] &8[&7*&8]", "available args = stats, version, gui"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/uptime &aget the machine uptime! &8[&7*&8]", "get the total uptime of the machine"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/devices &aget devices list! &8[&7*&8]", "get every attached device"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/cpuload &aget the CPU load! &8[&7*&8]", "Get CPU current percentage load"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/speedtest &aBenchmark your network! &8[&7*&8]", "Get your download\\upload speeds"));
            player.spigot().sendMessage(Utils.builderHover("&f- &7/java &aGet information about Java! &8[&7*&8]", "Show Version, Brand, Args and PID of JVM."));
            sender.sendMessage(Utils.color("&7&l&m--------------------------------------"));
        } else {
            sender.sendMessage(Utils.color("&7&l&m--------------------------------------"));
            sender.sendMessage(Utils.color("&f- &7/htop &aget processes list!"));
            sender.sendMessage(Utils.color("&f- &7/systeminfo [reload&f|&7version&f|&7stats&f|&7gui]"));
            sender.sendMessage(Utils.color("&f- &7/uptime &aget the machine uptime!"));
            sender.sendMessage(Utils.color("&f- &7/devices &aget devices list!"));
            sender.sendMessage(Utils.color("&f- &7/cpuload &aget the CPU load!"));
            sender.sendMessage(Utils.color("&f- &7/speedtest &aBenchmark your network!"));
            sender.sendMessage(Utils.color("&f- &7/java &aGet information about Java!"));
            sender.sendMessage(Utils.color("&7&l&m--------------------------------------"));
        }
    }

    /**
     * Calculates the size of a folder and returns it in bytes.
     *
     * @param folder The folder for which to calculate the size.
     * @return The size of the folder in bytes, or -1 if an error occurs.
     */
    public long folderFileSize(File folder) {
        if (folder == null) return -1;

        try (Stream<Path> paths = Files.walk(folder.toPath())) {
            return paths.mapToLong(p -> p.toFile().length()).sum();
        } catch (IOException ioException) {
            systemInfo.getLogger().warning("An exception has occurred while calculating the folder size of " + folder.getAbsolutePath());
            systemInfo.getLogger().warning(ioException.getLocalizedMessage());
        }

        return -1L;
    }

    /**
     * Displays server statistics to the command sender.
     *
     * @param sender The command sender.
     */
    private void stats(@NotNull CommandSender sender) {
        sender.sendMessage(Utils.color("&2» &7Server stats &2«"));
        sender.sendMessage(Utils.color("&2» &7Overworld Entities: &a" + Utils.countEntitiesInWorlds(NORMAL) + " &7Loaded Chunks: &a" + Utils.loadedChunksInWorlds(NORMAL)));
        sender.sendMessage(Utils.color("&2» &7Nether Entities: &a" + Utils.countEntitiesInWorlds(NETHER) + " &7Loaded Chunks: &a" + Utils.loadedChunksInWorlds(NETHER)));
        sender.sendMessage(Utils.color("&2» &7End Entities: &a" + Utils.countEntitiesInWorlds(THE_END) + " &7Loaded Chunks: &a" + Utils.loadedChunksInWorlds(THE_END)));
        sender.sendMessage(Utils.color("&2» &7Server File Size: &a" + Utils.formatData(folderFileSize(systemInfo.getServer().getWorldContainer()))));
    }

    /**
     * Reloads system values and informs the sender of the successful reload.
     *
     * @param sender The command sender.
     */
    private void reload(@NotNull CommandSender sender) {
        Utils.cachedColor = null;
        Utils.cachedDarkColor = null;
        systemInfo.getSystemInfoConfig().reloadValues();
        sender.sendMessage(Utils.color("&8» &aSuccessfully reloaded system values and config!"));
    }
}
