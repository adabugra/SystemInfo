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

package top.cmarco.systeminfo.commands.cpuload;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import top.cmarco.systeminfo.commands.SystemInfoCommand;
import top.cmarco.systeminfo.plugin.SystemInfo;
import top.cmarco.systeminfo.enums.Messages;
import top.cmarco.systeminfo.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import oshi.util.Util;
/**
 * The `CommandCpuLoad` class is a Spigot command that allows players with the appropriate permission to retrieve
 * information about the CPU load using the "/cpuload" command.
 */
public final class CommandCpuLoad extends SystemInfoCommand {

    // Stores previous CPU tick values for load calculation
    private long[] previousTicks;
    private long[][] previousMultiTicks;

    /**
     * Initializes a new instance of the `CommandCpuLoad` class.
     *
     * @param systemInfo The `SystemInfo` instance associated with this command.
     */
    public CommandCpuLoad(@NotNull SystemInfo systemInfo) {
        super(systemInfo, "cpuload",
                "gets the load status of the CPU",
                "/<command>",
                Collections.emptyList());
    }

    /**
     * Executes the "/cpuload" command, displaying the CPU load information to the sender.
     *
     * @param sender The command sender.
     * @param s      The command name.
     * @param args   The command arguments (not used in this command).
     * @return True if the command was executed successfully; otherwise, false.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("systeminfo.commands.cpuload")) {
                printCpuLoad(sender);
                return true;
            } else {
                sender.sendMessage(Messages.NO_PERMISSIONS.value(true));
            }
        } else {
            sender.sendMessage(Messages.OUT_OF_ARGS.value(true));
        }
        return false;
    }

    /**
     * Asynchronously retrieves the CPU load and calculates the system's average CPU load.
     *
     * @return A CompletableFuture that holds the CPU load percentage.
     */
    @NotNull
    private CompletableFuture<Double> getCpuLoad() {
        final CompletableFuture<Double> future = new CompletableFuture<>();
        final BukkitScheduler scheduler = systemInfo.getServer().getScheduler();

        scheduler.runTaskAsynchronously(systemInfo, () -> {
            previousTicks = systemInfo.getSystemValues().getSystemCpuLoadTicks();
            previousMultiTicks = systemInfo.getSystemValues().getProcessorCpuLoadTicks();
            Util.sleep(1000);
            scheduler.runTask(systemInfo, () -> future.complete(systemInfo.getSystemValues().getSystemCpuLoadBetweenTicks(previousTicks) * 100));
        });

        return future;
    }

    /**
     * Asynchronously retrieves the average CPU load per core.
     *
     * @return A CompletableFuture that holds the string representation of average CPU loads.
     */
    @NotNull
    private CompletableFuture<String> getAverageLoads() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final BukkitScheduler scheduler = systemInfo.getServer().getScheduler();
    
        scheduler.runTaskAsynchronously(systemInfo, () -> {
            StringBuilder cpuLoads = new StringBuilder("&7Load per core: \n");
    
            double[] load = systemInfo.getSystemValues().getLastCpuLoads();
            for (int i = 0; i < load.length; i++) {
                
                final double iLoad = load[i];
                final StringBuilder tempBuilder = new StringBuilder(String.format("&f&lC&r%d&7[", i));

                for (int j = 0; j < 10; j++) {
                    if ((j+1)*0.1d <= iLoad) {
                        tempBuilder.append("&c|");
                    } else {
                        tempBuilder.append("&a|");
                    }

                }

                tempBuilder.append("&7] ").append(String.format("&7%.2f", 100*iLoad)).append('%');

                cpuLoads.append(tempBuilder.toString());

                cpuLoads.append("   ");

                if ((i+1) % 4 == 0) {
                    cpuLoads.append('\n');
                }

            }
    
            scheduler.runTask(systemInfo, () -> future.complete(cpuLoads.toString()));
        });
    
        return future;
    }

    /**
     * Displays CPU load information to the sender.
     *
     * @param sender The command sender.
     */
    private void printCpuLoad(@NotNull CommandSender sender) {
        sender.sendMessage(Utils.color("&2» &7System load: &2«"));

        getCpuLoad().thenAcceptBothAsync(getAverageLoads(), (cpuload, loads) -> {
            sender.sendMessage(Utils.color(String.format("&7Cpu load: &a%.2f%%", cpuload)));
            sender.sendMessage(Utils.color(loads));
        });
    }
}