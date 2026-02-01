package org.cloudburstmc.netty.handler.codec.query;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Value;
import lombok.experimental.NonFinal;

public interface QueryEventListener {

    Data onQuery(InetSocketAddress address);

    @Value
    class Data {
        private final String hostname;
        private final String gametype;
        private final String map;
        private final int playerCount;
        private final int maxPlayerCount;
        private final int hostport;
        private final String hostip;
        private final String gameId;
        private final String version;
        private final String softwareVersion;
        private final boolean whitelisted;
        private final String[] plugins;
        private final String[] players;
        @NonFinal
        private transient ByteBuf longStats;
        @NonFinal
        private transient ByteBuf shortStats;

        public ByteBuf getLongStats() {
            if (longStats != null) {
                return longStats;
            }

            longStats = ByteBufAllocator.DEFAULT.buffer();

            longStats.writeBytes(QueryUtil.LONG_RESPONSE_PADDING_TOP);

            StringJoiner plugins = new StringJoiner(";");
            if (this.plugins != null) {
                for (String plugin : this.plugins) {
                    plugins.add(plugin);
                }
            }

            Map<String, String> kvs = new HashMap<>();
            kvs.put("hostname", hostname);
            kvs.put("gametype", gametype);
            kvs.put("map", map);
            kvs.put("numplayers", Integer.toString(playerCount));
            kvs.put("maxplayers", Integer.toString(maxPlayerCount));
            kvs.put("hostport", Integer.toString(hostport));
            kvs.put("hostip", hostip);
            kvs.put("game_id", gameId);
            kvs.put("version", version);
            kvs.put("plugins", softwareVersion + plugins.toString());
            kvs.put("whitelist", whitelisted ? "on" : "off");

            kvs.forEach((key, value) -> {
                QueryUtil.writeNullTerminatedString(longStats, key);
                QueryUtil.writeNullTerminatedString(longStats, value);
            });

            longStats.writeByte(0);
            longStats.writeBytes(QueryUtil.LONG_RESPONSE_PADDING_BOTTOM);

            if (players != null) {
                for (String player : players) {
                    QueryUtil.writeNullTerminatedString(longStats, player);
                }
            }
            longStats.writeByte(0);

            return longStats;
        }

        public ByteBuf getShortStats() {
            if (shortStats != null) {
                return shortStats;
            }

            shortStats = ByteBufAllocator.DEFAULT.buffer();

            QueryUtil.writeNullTerminatedString(shortStats, hostname);
            QueryUtil.writeNullTerminatedString(shortStats, gametype);
            QueryUtil.writeNullTerminatedString(shortStats, map);
            QueryUtil.writeNullTerminatedString(shortStats, Integer.toString(playerCount));
            QueryUtil.writeNullTerminatedString(shortStats, Integer.toString(maxPlayerCount));
            shortStats.writeShortLE(hostport);
            QueryUtil.writeNullTerminatedString(shortStats, hostip);

            return shortStats;
        }
    }
}
