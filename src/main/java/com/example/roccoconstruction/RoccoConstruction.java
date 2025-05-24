package com.example.roccoconstruction;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.Collections.query.WidgetQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.example.roccodriftnet.RoccoDriftNetsConfig;
import com.example.EthanApiPlugin.Utility.DelayedActionExecutor;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.Optional;


@PluginDescriptor(name = "RoccoConstruction", description = "", enabledByDefault = false, tags = {"Testing"})
public class RoccoConstruction extends Plugin {

    @Inject
    private Client client;


    @Inject
    private EventBus eventBus;

    DelayedActionExecutor executor;

    @Override
    protected void startUp() throws Exception {
        executor = new DelayedActionExecutor(eventBus);
    }


    @Subscribe
    public void onGameTick(GameTick event) {
        Optional<NPC> butler = NPCs.search().withAction("Talk-to").nearestToPlayer();


        if(butler.isPresent() && !sendButlerThreshhold()) {
            if(butler.get().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) != 1) {
                return;
            }
            MousePackets.queueClickPacket();
            NPCInteraction.interact(butler.get(), "Talk-to");
            executor.schedule( () -> {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause((219 << 16) | 1, 1);
            }, 1);
            return;
        }



        Optional<TileObject> oakLarderRemove = TileObjects.search().withId(ObjectID.POH_LARDER_2).withAction("Remove").nearestToPlayer();
        if (oakLarderRemove.isPresent()) {
            MousePackets.queueClickPacket();
            MousePackets.queueClickPacket();
            TileObjectInteraction.interact(oakLarderRemove.get(), "Remove");
            executor.schedule( () -> {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause((219 << 16) | 1, 1);
            }, 1);
            return;
        }

        Optional<TileObject> oakLarderBuild = TileObjects.search().withId(15403).nearestToPlayer();
        if (oakLarderBuild.isPresent() && hasEnoughOakPlanksInInventory()) {
            MousePackets.queueClickPacket();
            MousePackets.queueClickPacket();
            TileObjectInteraction.interact(oakLarderBuild.get(), "Build");
            executor.schedule( () -> {
                MousePackets.queueClickPacket();
                WidgetPackets.queueResumePause((458 << 16) | 2, 2);
            }, 1);
            return;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if(event.getType() == ChatMessageType.DIALOG && event.getMessage().contains("if thou desirest my")) {
            WidgetPackets.queueResumePause((231 << 16) | 5, -1);
            WidgetPackets.queueResumePause((219 << 16) | 1, 1);
        }
        if(event.getType() == ChatMessageType.DIALOG && event.getMessage().contains("conjure items")) {
            EthanApiPlugin.stopPlugin(this);
        }
    }


    private boolean hasEnoughOakPlanksInInventory() {
        return Inventory.getItemAmount(ItemID.OAK_PLANK) >= 8;
    }

    private boolean sendButlerThreshhold() {
        return Inventory.getItemAmount(ItemID.OAK_PLANK) >= 17;
    }


}