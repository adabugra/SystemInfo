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

package top.cmarco.systeminfo.gui;

import com.google.common.collect.ImmutableList;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import space.arim.morepaperlib.scheduling.ScheduledTask;
import top.cmarco.systeminfo.oshi.SystemValues;
import top.cmarco.systeminfo.plugin.SystemInfo;
import top.cmarco.systeminfo.protocol.BukkitNetworkingManager;
import top.cmarco.systeminfo.protocol.NetworkStatsData;
import top.cmarco.systeminfo.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * This class is used for the interactive GUI in the systeminfo gui command.
 * It displays basic information using Minecraft blocks and animations.
 */
public final class SystemInfoGui {

    public SystemInfoGui(@NotNull SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    private final SystemInfo systemInfo;
    private final Map<UUID, ScheduledTask> tasks = new HashMap<>();

    /* ---------------------------- */
    private static final List<Integer> BACKGROUND_SLOTS = ImmutableList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 26, 25, 24, 23, 22, 21, 20, 19, 10);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("d\\M\\u h:m:s a");
    public static final Inventory GUI = Bukkit.createInventory(null, 9*3, "SystemInfo");

    /* ---------------------------- */

    private ScheduledTask fillTask = null;

    /* ---------------------------- */

    /**
     * This method creates the GUI to a Player
     *
     * @param player a valid Player
     */
    public void createGui(@NotNull Player player) {
        SystemInfo.morePaperLib.scheduling().entitySpecificScheduler(player).run(() -> {
            this.cleanBackground();
            this.fillBackground();
            player.openInventory(GUI);
        }, null);


        final ScheduledTask scheduledTask = SystemInfo.morePaperLib.scheduling().entitySpecificScheduler(player).runAtFixedRate(() -> {

            final InventoryView inventoryView = player.getOpenInventory();

            if (inventoryView.getTopInventory().equals(GUI)) {

                this.updateInventory();

            } else {
                ScheduledTask bt = this.tasks.get(player.getUniqueId());

                if (bt != null) {
                    bt.cancel();
                    this.tasks.remove(player.getUniqueId());
                }

            }
        }, null, 2L, 20L);

        this.tasks.put(player.getUniqueId(), scheduledTask);
    }

    /**
     * This method generates an animation that consists in taking a list of integers that represents
     * inventory slots, then generating items with material parameter for each slot creating a cool effect
     */
    private void fillBackground() {
        final Iterator<Integer> invSlotIterator = SystemInfoGui.BACKGROUND_SLOTS.iterator();

        this.fillTask = SystemInfo.morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(() -> {

            if (invSlotIterator.hasNext()) {

                setCustomItem(SystemInfoGui.GUI, Material.STAINED_GLASS_PANE, invSlotIterator.next(), " ", " ");

            } else {

                if (this.fillTask != null) {
                    this.fillTask.cancel();
                    this.fillTask = null;
                }
            }

        }, 1L, 1L);
    }

    private void cleanBackground() {
        for (int backgroundSlot : SystemInfoGui.BACKGROUND_SLOTS) {
            final int slot = backgroundSlot - 1;
            SystemInfoGui.GUI.setItem(slot, null);
        }
    }

    /**
     * This method creates and sets in an inventory a new custom ItemStack from the given parameters:
     *
     * @param inv         this is the inventory where the item should be set.
     * @param material    this is the material that the new ItemStack will have.
     * @param invSlot     this is in which slot of the inventory the item will be set.
     * @param displayName the display name of the new ItemStack (this does support color codes with &).
     * @param loreText    the lore of the new ItemStack (this does support color codes with & and multiple lines with \n).
     * @throws IllegalArgumentException if amount of items is illegal, or the slot is illegal.
     */
    public static void setCustomItem(@NotNull Inventory inv, @NotNull Material material, int invSlot, @NotNull String displayName, String... loreText) {
        if ((invSlot >= 0 && invSlot <= inv.getSize())) {
            ItemStack item;
            List<String> lore = new ArrayList<>();
            item = new ItemStack(material, 1);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(Utils.color(displayName));
            for (String s : loreText) {
                lore.add(Utils.color(s));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            inv.setItem(invSlot - 1, item);
        }
    }

    /**
     * This method updates the inventory with new items
     */
    public void updateInventory() {
        SystemValues values = this.systemInfo.getSystemValues();
        setCustomItem(GUI, Material.GREEN_RECORD, 11, "&2Processor",
                "&7Vendor: &a" + values.getCpuVendor(),
                "&7Model: &a" + values.getCpuModel() + " " + values.getCpuModelName(),
                "&7Clock Speed: &a" + values.getCpuMaxFrequency() + " GHz",
                "&7Physical Cores: &a" + values.getCpuCores(),
                "&7Logical Cores: &a" + values.getCpuThreads());

        setCustomItem(GUI, Material.RECORD_10, 12, "&2CPU Load",
                "&7Global Load: &a" + String.format("%.2f", values.getLastCpuLoad()) + "%");

        setCustomItem(GUI, Material.RECORD_4, 13, "&2Memory",
                "&7Total: &a" + values.getMaxMemory(),
                "&7Available: &a" + values.getAvailableMemory(),
                "&7Swap Used: &a" + values.getUsedSwap(),
                "&7Swap Allocated: &a" + values.getTotalSwap());

        setCustomItem(GUI, Material.RECORD_6, 14, "&2GPU",
                "&7GPU Model: &a" + values.getMainGPU().getName(),
                "&7GPU Vendor: &a" + values.getMainGPU().getVendor(),
                "&7GPU VRAM: &a" + Utils.formatData(values.getMainGPU().getVRam()));

        setCustomItem(GUI, Material.GOLD_RECORD, 15, "&2Operating system",
                "&7Name: &a" + values.getOSFamily() + " " + values.getOSManufacturer(),
                "&7Version: &a" + values.getOSVersion(),
                "&7Active Processes: &a" + values.getRunningProcesses());

        BukkitNetworkingManager networkingManager = systemInfo.getNetworkingManager();

        if (networkingManager != null) {

            final NetworkStatsData networkStatsData = networkingManager.getNetworkStats();

            setCustomItem(GUI, Material.RECORD_3, 16, "&2Networking",
                    "&7Name: &a" + values.getNetworkInterfaceName(),
                    "&7Packets Out: &a" + networkStatsData.getLastSentPackets() + "/s",
                    "&7Packets In: &a" + networkStatsData.getLastReceivedPackets() + "/s",
                    "&7Packets Out Total: &a" + networkStatsData.getTotalSentPackets(),
                    "&7Packets In Total: &a" + networkStatsData.getTotalReceivedPackets(),
                    "&7Data Out Total: &a" + Utils.formatData(networkStatsData.getTotalSentBytes()),
                    "&7Data In Total: &a" + Utils.formatData(networkStatsData.getTotalReceivedBytes()),
                    "&7Data Out: &a" + Utils.formatData(networkStatsData.getLastSentBytes()) + "/s",
                    "&7Data In: &a" + Utils.formatData(networkStatsData.getLastReceivedBytes()) + "/s"
            );

        } else {

            setCustomItem(GUI, Material.RECORD_3, 16, "&cNetworking",
                    "&c! Missing PacketEvents-API !",
                    "&7In order to use this networking feature",
                    "&7you are required to install the correct",
                    "&7version of PacketEvents in your server.");

        }

        setCustomItem(GUI, Material.RECORD_9, 17, "&2Uptime",
                "&7Jvm uptime: &a" + ChronoUnit.MINUTES.between(systemInfo.getStartupTime(), LocalDateTime.now()) + " min.",
                "&7Current time: &a" + LocalDateTime.now().format(TIME_FORMATTER));


    }
}
