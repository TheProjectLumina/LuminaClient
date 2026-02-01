package org.cloudburstmc.netty.util;

import java.lang.reflect.Field;
import java.net.SocketOption;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.unix.IntegerUnixChannelOption;

public class IpDontFragmentProvider {
    private static final ChannelOption<?> IP_DONT_FRAGMENT_OPTION;
    private static final Object IP_DONT_FRAGMENT_TRUE_VALUE;
    private static final Object IP_DONT_FRAGMENT_FALSE_VALUE;

    static {
        ChannelOption<?> ipDontFragmentOption = null;
        Object ipDontFragmentTrueValue = null;
        Object ipDontFragmentFalseValue = null;
        
        setterBlock: {
            // Windows and Linux Compatible (Java 19+)
            try {
                Class<?> c = Class.forName("jdk.net.ExtendedSocketOptions");
                Field f = c.getField("IP_DONTFRAGMENT");
    
                ipDontFragmentOption = NioChannelOption.of((SocketOption<?>) f.get(null));
                ipDontFragmentTrueValue = true;
                ipDontFragmentFalseValue = false;
                break setterBlock;
            } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                
            }

            // Unix Compatible (Java 8+)
            ipDontFragmentOption = new IntegerUnixChannelOption("IP_DONTFRAG", 0 /* IPPROTO_IP */, 10 /* IP_MTU_DISCOVER */);
            ipDontFragmentTrueValue = 2 /* IP_PMTUDISC_DO */;
            ipDontFragmentFalseValue = 0 /* IP_PMTUDISC_DONT */;
            break setterBlock;
        }

        IP_DONT_FRAGMENT_OPTION = ipDontFragmentOption;
        IP_DONT_FRAGMENT_TRUE_VALUE = ipDontFragmentTrueValue;
        IP_DONT_FRAGMENT_FALSE_VALUE = ipDontFragmentFalseValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> boolean trySet(Channel channel, boolean value) {
        if (IP_DONT_FRAGMENT_OPTION == null) return false;
        boolean success = channel.config().setOption((ChannelOption<T>) IP_DONT_FRAGMENT_OPTION, (T) (value ? IP_DONT_FRAGMENT_TRUE_VALUE : IP_DONT_FRAGMENT_FALSE_VALUE));
        return success ? value : !value;
    }
}