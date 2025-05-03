package com.example.roccodriftnetcrafter;


import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.Collections.query.TileObjectQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@PluginDescriptor(name = "RoccoDriftNetCrafter", description = "", enabledByDefault = false, tags = {"Testing"})
public class RoccoDriftNetCrafter extends Plugin {

    @Inject
    private Client client;

    private boolean isInteractingWithLoom  = false;
    private int ticksSinceLastLoomClick = 0;


    @Subscribe
    private void onGameTick(GameTick event) {
        if (client.getLocalPlayer().getAnimation() != -1) {
            return;
        }

        if (isInteractingWithLoom) {
            ticksSinceLastLoomClick++;
            // After 10 ticks (~6 seconds), assume interaction failed and reset
            if (ticksSinceLastLoomClick > 10) {
                isInteractingWithLoom = false;
            }
            return;
        }

        if(isBankOpen()) {
            Widget depositInventory = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
            if (depositInventory != null) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(depositInventory, "Deposit inventory");
            }
            Bank.search().withName("Jute fibre").first().ifPresentOrElse(item -> {
                BankInteraction.withdrawX(item, 28);
            }, () -> {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No Jute fibres in bank!", null);
                EthanApiPlugin.stopPlugin(this);
            });
        }

        if (!Inventory.search().withName("Jute fibre").empty()) {
            TileObject loom = TileObjects.search().withAction("Weave").first().orElse(null);
            if (loom != null) {
                MousePackets.queueClickPacket();
                TileObjectInteraction.interact(loom, "Weave");
                // Mark that we just clicked the loom and reset the counter
                isInteractingWithLoom = true;
                ticksSinceLastLoomClick = 0;
            }
        } else {
            findBank();
        }

    }


    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == 270) {

            Widget driftNet = client.getWidget(270, 16);
            Widget[] children = driftNet.getDynamicChildren();

            Optional<Widget> driftnet = Widgets.search().hiddenState(false).withAction("Make").first();
            

            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ""+ driftnet.get().getId(), null);

            //Widget realnigga = Arrays.stream(children).filter(child -> child.getName().contains("Drift net") == true).collect(Collectors.toList())[0];





        }
    }



    private void findBank(){
        Optional<TileObject> chest = TileObjects.search().withName("Bank chest").nearestToPlayer();
        Optional<NPC> banker = NPCs.search().withAction("Bank").nearestToPlayer();
        Optional<TileObject> booth = TileObjects.search().withAction("Bank").nearestToPlayer();
        if (chest.isPresent()){
            TileObjectInteraction.interact(chest.get(), "Use");
            return;
        }
        if (booth.isPresent()){
            TileObjectInteraction.interact(booth.get(), "Bank");
            return;
        }
        if (banker.isPresent()){
            NPCInteraction.interact(banker.get(), "Bank");
            return;
        }
        if (!chest.isPresent() && !booth.isPresent() && !banker.isPresent()){
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "couldn't find bank or banker", null);
            EthanApiPlugin.stopPlugin(this);
        }
    }
    private void pressSpace() {
        KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyPress);
        KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyRelease);
        KeyEvent keyTyped = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyTyped);
    }

    private boolean hasJuteInBank() {
        return Bank.search().nameContains("Jute fibre").first().isPresent();
    }

    private boolean hasJuteInInventory() {
        return Inventory.search().nameContains("Jute fibre").first().isPresent();
    }

    private boolean isBankOpen() {
        Widget bank = client.getWidget(WidgetInfo.BANK_CONTAINER);
        return bank != null && !bank.isHidden();
    }

}