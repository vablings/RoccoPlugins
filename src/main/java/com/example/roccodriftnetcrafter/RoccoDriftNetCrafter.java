package com.example.roccodriftnetcrafter;


import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import com.example.EthanApiPlugin.Collections.query.TileObjectQuery;
import com.example.EthanApiPlugin.Collections.query.WidgetQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.example.rocconaugas.RoccoNaugas;
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

    private int idleTicks = 0;


    private ItemQuery driftNetsInBank;
    private ItemQuery juteInBank;
    private ItemQuery juteInInventory;



    public State state = State.BANKING_STAMINA;

    enum State {
        CRAFTING,
        GOING_T0_BANK,
        BANKING,
        BANKING_STAMINA,
        RETURNING,
        STUCK,
    }


    @Override
    protected void startUp() throws Exception {
        state = State.BANKING;
    }

    private void evaluteState() {
        switch(state) {
            case CRAFTING:
                if(!client.getLocalPlayer().isInteracting() && juteInInventory.result().isEmpty()) {

                    state = state.GOING_T0_BANK;
                }
                break;
            case GOING_T0_BANK:
                Widget bank = client.getWidget(WidgetInfo.BANK_CONTAINER);
                if(bank != null && !bank.isHidden()  ) {
                    state = state.BANKING;
                }
                break;
            case BANKING:
                if(juteInInventory.result().size() >= 28) {
                    state = state.RETURNING;
                }
                break;
            case RETURNING:
                Optional<Widget> craftingInterface = Widgets.search().withTextContains("many do you").first();
                if(craftingInterface.isPresent()) {
                    state = State.CRAFTING;
                }
                break;
        }
    }
    private void updateValues() {
        driftNetsInBank = Bank.search().nameContains("Drift net");
        juteInBank = Bank.search().nameContains("Jute fibre");
        juteInInventory = Inventory.search().nameContains("Jute fibre");

    }


    private void executeState() {
        if(EthanApiPlugin.isMoving()) {
            return;
        }

        switch(state) {
            case CRAFTING:
                if (client.getLocalPlayer().getAnimation() != -1) {
                    idleTicks++;
                }
                if(idleTicks > 2 ) {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueResumePause((270 << 16) | 16, 14);
                }
                break;
            case GOING_T0_BANK:
                findBank();
                break;
            case BANKING:
                Widget depositInventory = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
                if (depositInventory != null) {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(depositInventory, "Deposit inventory");
                }
                Bank.search().withName("Jute fibre").first().ifPresentOrElse(item -> {
                    MousePackets.queueClickPacket();
                    BankInteraction.useItem(item, "Withdraw-all");
                }, () -> {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No Jute fibres in bank!", null);
                    EthanApiPlugin.stopPlugin(this);
                });
                break;

            case BANKING_STAMINA:
                Bank.search().withName("Stamina").first().ifPresentOrElse(item -> {
                    MousePackets.queueClickPacket();
                    BankInteraction.useItem(item, "Withdraw-1");
                    Optional<Widget> stam = Inventory.search().nameContains("Stamina").withAction("Drink").first();
                    stam.ifPresent(widget -> InventoryInteraction.useItem(widget, "Drink"));
                }, () -> {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No Jute fibres in bank!", null);
                    EthanApiPlugin.stopPlugin(this);
                });

                Widget depositInventoryStam = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
                if (depositInventoryStam != null) {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(depositInventoryStam, "Deposit inventory");
                }

                Bank.search().withName("Jute fibre").first().ifPresentOrElse(item -> {
                    MousePackets.queueClickPacket();
                    BankInteraction.useItem(item, "Withdraw-all");
                }, () -> {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No Jute fibres in bank!", null);
                    EthanApiPlugin.stopPlugin(this);
                });
                break;
            case RETURNING:
                TileObject loom = TileObjects.search().withAction("Weave").first().orElse(null);
                if (loom != null) {
                    MousePackets.queueClickPacket();
                    TileObjectInteraction.interact(loom, "Weave");
                }
                break;
        }


    }




    @Subscribe
    private void onGameTick(GameTick event) {
        updateValues();
        evaluteState();
        executeState();
        System.out.println("" + state);
    }


    private void findBank(){
        Optional<TileObject> chest = TileObjects.search().withName("Bank chest").nearestToPlayer();
        Optional<NPC> banker = NPCs.search().withAction("Bank").nearestToPlayer();
        Optional<TileObject> booth = TileObjects.search().withAction("Bank").nearestToPlayer();
        if (chest.isPresent()){
            MousePackets.queueClickPacket();
            TileObjectInteraction.interact(chest.get(), "Use");
            return;
        }
        if (booth.isPresent()){
            MousePackets.queueClickPacket();
            TileObjectInteraction.interact(booth.get(), "Bank");
            return;
        }
        if (banker.isPresent()){
            MousePackets.queueClickPacket();
            NPCInteraction.interact(banker.get(), "Bank");
            return;
        }
        if (!chest.isPresent() && !booth.isPresent() && !banker.isPresent()){
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "couldn't find bank or banker", null);
            EthanApiPlugin.stopPlugin(this);
        }
    }


    private boolean isBankOpen() {
        Widget bank = client.getWidget(WidgetInfo.BANK_CONTAINER);
        return bank != null && !bank.isHidden();
    }

}