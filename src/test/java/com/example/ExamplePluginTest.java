package com.example;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.roccodriftnet.RoccoDriftNets;
import com.example.roccodriftnetcrafter.RoccoDriftNetCrafter;
import com.example.roccofastteaks.RoccoFastTeaks;
import com.example.roccoutils.RoccoUtil;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(EthanApiPlugin.class, PacketUtilsPlugin.class, RoccoUtil.class, RoccoFastTeaks.class, RoccoDriftNets.class, RoccoDriftNetCrafter.class);
        RuneLite.main(args);
    }
}


