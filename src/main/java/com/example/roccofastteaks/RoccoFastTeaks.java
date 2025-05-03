package com.example.roccofastteaks;

import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Utility.WorldPointUtility;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.example.PathingTesting.PathingTestingConfig;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import com.google.inject.Inject;
import java.util.Optional;
import java.util.*;


@PluginDescriptor(name = "RoccoFastTeaks", description = "", enabledByDefault = true, tags = {"Testing"})
public class RoccoFastTeaks extends Plugin {
    @Inject
    Client client;

    /*
    @Inject
    RoccoUtilConfig config;

    @Provides
    public PathingTestingConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PathingTestingConfig.class);
    }
     */

    WorldPoint treeWest_loc1 = new WorldPoint(3704, 3836, 0);
    WorldPoint treeWest_loc2 = new WorldPoint(3704, 3838, 0);

    WorldPoint treeSouth_loc1 = new WorldPoint(3706, 3834, 0);
    WorldPoint treeSouth_loc2 = new WorldPoint(3707, 3835, 0);

    /*
    Ok heres the magic
    The south tree west tiles are only occupied when two conditions are met
    1. the west tree has just falled
    2. the west tree is just about to respawn

    otherwise we use the east tiles on the south tree and then if thats felled we go to the east tree

    todo()
     */
    private int ticksOnCurrentTile = 0;
    private WorldPoint lastTile  = null;

    @Subscribe
    public void onGameTick(GameTick event) {
        Optional<TileObject> treeWest = TileObjects.search().withAction("Chop Down").withId(30481).first();
        Optional<TileObject> treeSouth = TileObjects.search().withAction("Chop Down").withId(30480).first();
        Optional<TileObject> treeEast = TileObjects.search().withAction("Chop Down").withId(30482).first();

        stamIfRequired();
        specIfAvailable();

        if (treeWest.isPresent()) {
            tickManipMove(treeWest_loc1, treeWest_loc2);
            if (ticksOnCurrentTile >= 2) {
                TileObjectInteraction.interact(treeWest.get(), "Chop Down");
            }
        } else if (treeSouth.isPresent()) {
            tickManipMove(treeSouth_loc1, treeSouth_loc2);
            if (ticksOnCurrentTile >= 2) {
                TileObjectInteraction.interact(treeSouth.get(), "Chop Down");
            }
        }


        dropLogs();

    }


    private void dropLogs() {
        List<Widget> logs = Inventory.search().nameContains("Teak logs").result();
        for(int i = 0; i < 3; i++) {
            InventoryInteraction.useItem(logs.get(i), "Drop");
        }
    }

    private void stamIfRequired() {
        if (client.getEnergy() > 5000 || (client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 1 )) {
            return;
        }
        Widget stam = Inventory.search().nameContains("Stamina").result().get(0);
        MousePackets.queueClickPacket();
        InventoryInteraction.useItem(stam, "Drink");
    }

    private void specIfAvailable() {
        if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000) {
            if(!Equipment.search().matchesWildCardNoCase("Dragon axe").empty())
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, 10747973, -1, -1);
        }
    }

    private void tickManipMove(WorldPoint tileA, WorldPoint tileB) {
        WorldPoint currentLocation = client.getLocalPlayer().getWorldLocation();
        if (lastTile != null && currentLocation.equals(lastTile)) {
            ticksOnCurrentTile++;
        } else {
            ticksOnCurrentTile = 1;
            lastTile = currentLocation;
        }

        if (ticksOnCurrentTile >= 3) {
            WorldPoint targetLocation = currentLocation.equals(tileA) ? tileB : tileA;

            tickManip();
            MousePackets.queueClickPacket();
            MovementPackets.queueMovement(targetLocation);

            lastTile = targetLocation; // update the target as the new last tile
            ticksOnCurrentTile = 0;
        }
    }

    private void tickManip() {
        Optional<Widget> mahoganyLogs = Inventory.search().withId(ItemID.MAHOGANY_LOGS).first();
        Optional<Widget> knife = Inventory.search().withId(ItemID.KNIFE).first();

        if (mahoganyLogs.isEmpty() || knife.isEmpty()) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Missing mahogany logs or knife", null);
            EthanApiPlugin.stopPlugin(this);
            return;
        }
        MousePackets.queueClickPacket();
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetOnWidget(mahoganyLogs.get(), knife.get());
    }



}


