package com.example;

import com.example.AutoSmith.AutoSmith;
import com.example.E3T4G.et34g;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Firemaking.FiremakingPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.roccoconstruction.RoccoConstruction;
import com.example.roccodriftnet.RoccoDriftNets;
import com.example.roccodriftnetcrafter.RoccoDriftNetCrafter;
import com.example.roccofastteaks.RoccoFastTeaks;
import com.example.roccogotr.RoccoGotr;
import com.example.rocconaugas.RoccoNaugas;
import com.example.roccoutils.RoccoUtil;
import com.example.roccowildyboner.RoccoBoner;
import com.example.roccoyoinker.Yoinker;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(EthanApiPlugin.class,
                PacketUtilsPlugin.class,
                RoccoUtil.class,
                RoccoFastTeaks.class,
                RoccoDriftNets.class,
                RoccoDriftNetCrafter.class,
                RoccoConstruction.class,
                RoccoNaugas.class,
                et34g.class,
                FiremakingPlugin.class,
                AutoSmith.class,
                Yoinker.class,
                RoccoGotr.class,
                RoccoBoner.class
        );
        RuneLite.main(args);
    }
}


