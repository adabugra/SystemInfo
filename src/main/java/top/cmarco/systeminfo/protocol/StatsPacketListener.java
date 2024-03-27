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

package top.cmarco.systeminfo.protocol;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public final class StatsPacketListener implements PacketListener {

    private final BukkitNetworkingManager bukkitNetworkingManager;

    public StatsPacketListener(@NotNull final BukkitNetworkingManager bukkitNetworkingManager) {
        this.bukkitNetworkingManager = bukkitNetworkingManager;
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final ByteBuf byteBuf = (ByteBuf) event.getByteBuf();
        final int bytes = byteBuf.readableBytes();

        if (bytes <= 0x00) {
            return;
        }

        bukkitNetworkingManager.totalReceivedBytes += bytes;
        bukkitNetworkingManager.lastReceivedBytes += bytes;
        ++bukkitNetworkingManager.lastReceivedPackets;
        ++bukkitNetworkingManager.totalReceivedPackets;
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        final ByteBuf byteBuf = (ByteBuf) event.getByteBuf();
        final int bytes = byteBuf.readableBytes();

        if (bytes <= 0x00) {
            return;
        }

        bukkitNetworkingManager.totalSentBytes += bytes;
        bukkitNetworkingManager.lastSentBytes += bytes;
        ++bukkitNetworkingManager.lastSentPackets;
        ++bukkitNetworkingManager.totalSentPackets;
    }

}
