package com.example.rocconaugas;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import com.example.EthanApiPlugin.Collections.query.NPCQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Utility.PrayerUtil;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Arrays;
import java.util.Optional;

@PluginDescriptor(name = "RoccoNaugas", description = "", enabledByDefault = true, tags = {"Testing"})
public class RoccoNaugas extends Plugin {
    @Inject
    Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private RoccoNaugasOverlay overlay;

    private WorldPoint nearExitDoor = new WorldPoint(1374, 9667, 0);
    private WorldPoint nearNaugas = new WorldPoint(1356, 9572, 0);
    private WorldPoint northOfRoom = new WorldPoint (1357, 9581, 0);
    private WorldPoint nearResupplyDoor = new WorldPoint (1347, 9590, 0);

    public ItemQuery moonlightPotions;
    public ItemQuery moonlightGrubs;
    public ItemQuery moonlightGrubPaste;

    public ItemQuery vialsOfWater;
    public ItemQuery pestleAndMortar;
    public NPCQuery sulphurNaugas;
    public boolean inResupplyArea;
    public int ticksSinceLastStateChange;
    public State previousState;

    public State state = State.RESUPPLY_GRINDING_GRUBBYS;

    @Override
    protected void startUp() throws Exception {
        state = State.EXITING_RESUPPLY;
        this.overlayManager.add(overlay);
    }

    public enum State {
        LOOKING_FOR_TARGET,
        FIGHTING,
        GOING_TO_NORTH_OF_ROOM,
        GOING_TO_RESUPPLY_AREA,
        RESUPPLY_GATHERING_GRUBBYS,
        RESUPPLY_DRINKING_TEA,
        RESUPPLY_GATHERING_POTIONS,
        RESUPPLY_GRINDING_GRUBBYS,
        RESUPPLY_MIXING,
        EXITING_RESUPPLY,
        GOING_TO_NAUGAS,
        STUCK_ERROR,
    }

    private void evaluateState() {
        int moonlightPotionCount = moonlightPotions != null
                && moonlightPotions.result() != null ?
                moonlightPotions.result().size() : 0;

        int moonlightGrubsCount = moonlightGrubs != null
                && moonlightGrubs.result() != null ?
                moonlightGrubs.result().size() : 0;

        int moonlightGrubPasteCount = moonlightGrubPaste != null
                && moonlightGrubPaste.result() != null ?
                moonlightGrubPaste.result().size() : 0;

        int vialsOfWaterCount = vialsOfWater != null
                && vialsOfWater.result() != null ?
                vialsOfWater.result().size() : 0;


        switch(state) {
            case FIGHTING:
                // If we run out of potions while fighting, go resupply
                if (moonlightPotionCount <= 2) {
                    state = State.GOING_TO_NORTH_OF_ROOM;
                }
                break;
            case GOING_TO_NORTH_OF_ROOM:
                // If we run out of potions while fighting, go resupply
                if (client.getLocalPlayer().getWorldLocation().getY() >= 9580) {
                    state = State.GOING_TO_RESUPPLY_AREA;
                }
                break;

            case GOING_TO_RESUPPLY_AREA:
                // Once we're in the resupply area, start gathering grubbys
                if (inResupplyArea && client.getLocalPlayer().getWorldLocation().equals(nearExitDoor)) {
                    state = State.RESUPPLY_GATHERING_GRUBBYS;
                }
                break;

            case RESUPPLY_GATHERING_GRUBBYS:
                // Once we have enough grubbys, move to gathering water vials
                if (moonlightGrubsCount >= 10) {
                    state = State.RESUPPLY_DRINKING_TEA;
                }
                break;

            case RESUPPLY_DRINKING_TEA:
                // Drink some tea if we are thirsty
                if (client.getEnergy() != 10000) {
                    state = State.RESUPPLY_GATHERING_POTIONS;
                }
                break;

            case RESUPPLY_GATHERING_POTIONS:
                // Once we have enough water vials, move to grinding grubbys
                if (vialsOfWaterCount >= 10) {
                    state = State.RESUPPLY_GRINDING_GRUBBYS;
                }
                break;

            case RESUPPLY_GRINDING_GRUBBYS:
                // Once all grubbys are ground into paste, move to mixing
                if (moonlightGrubsCount == 0 && moonlightGrubPasteCount > 0) {
                    state = State.RESUPPLY_MIXING;
                }
                break;

            case RESUPPLY_MIXING:
                // Once we have potions and no more materials to mix, return to fight
                if (moonlightPotionCount == 10) {
                    state = State.EXITING_RESUPPLY;
                }
                break;

            case EXITING_RESUPPLY:
                if (!inResupplyArea) {
                    state = State.GOING_TO_NAUGAS;
                }
                break;
        }
    }
    private void refreshValues() {
        moonlightPotions = Inventory.search().nameContains("Moonlight potion");
        moonlightGrubs = Inventory.search().withId(ItemID.MOONLIGHT_GRUB);
        moonlightGrubPaste = Inventory.search().withId(ItemID.MOONLIGHT_GRUB_PASTE);
        vialsOfWater = Inventory.search().withId(ItemID.VIAL_OF_WATER);
        pestleAndMortar = Inventory.search().withId(ItemID.PESTLE_AND_MORTAR);

        inResupplyArea = !NPCs.search().nameContains("Moonlight moth").empty();
        sulphurNaugas = NPCs.search().nameContains("Sulphur nauga");
    }

    private void executeState() {
        if(EthanApiPlugin.isMoving()) {
            return;
        }
        switch(state) {
            case GOING_TO_RESUPPLY_AREA:
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(nearResupplyDoor);
                if(client.getLocalPlayer().getWorldLocation().equals(nearResupplyDoor)) {
                    Optional<TileObject> returnDoor = TileObjects.search().withAction("Pass-through").nearestToPlayer();
                    if(returnDoor.isPresent()) {
                        MousePackets.queueClickPacket();
                        TileObjectInteraction.interact(returnDoor.get(), "Pass-through");
                    }
                }
                Optional<TileObject> door = TileObjects.search().withId(51375).withAction("Pass-through").nearestToPlayer();
                if(door.isPresent()) {
                    MousePackets.queueClickPacket();
                    TileObjectInteraction.interact(door.get(), "Pass-through");
                }
                break;
            case GOING_TO_NORTH_OF_ROOM:
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(northOfRoom);
                break;

            case RESUPPLY_GATHERING_GRUBBYS:
                if (client.getLocalPlayer().getAnimation() != -1) { return; }
                Optional<TileObject> grubbySapling = TileObjects.search().nameContains("Grubby").withAction("Collect-from").nearestToPlayer();
                if(grubbySapling.isPresent()) {
                    MousePackets.queueClickPacket();
                    TileObjectInteraction.interact(grubbySapling.get(), "Collect-From");
                }
                break;

            case RESUPPLY_DRINKING_TEA:
                Optional<TileObject> cookingStove = TileObjects.search().nameContains("Cooking stove").withAction("Make-cuppa").nearestToPlayer();
                if(cookingStove.isPresent()) {
                    MousePackets.queueClickPacket();
                    TileObjectInteraction.interact(cookingStove.get(), "Make-cuppa");
                }
                break;
            case RESUPPLY_GATHERING_POTIONS:

                Optional<TileObject> supplyCrate = TileObjects.search().withId(51371).nearestToPlayer();
                if(supplyCrate.isPresent()) {
                    MousePackets.queueClickPacket();
                    TileObjectInteraction.interact(supplyCrate.get(), "Take-from <col=00ffff>Herblore");
                }

                break;

            case RESUPPLY_GRINDING_GRUBBYS:
                if (client.getLocalPlayer().getAnimation() != -1) { return; }
                MousePackets.queueClickPacket();
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(moonlightGrubs.first().get(), pestleAndMortar.first().get());
                break;

            case RESUPPLY_MIXING:
                if (client.getLocalPlayer().getAnimation() != -1) { return; }
                MousePackets.queueClickPacket();
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(moonlightGrubPaste.first().get(), vialsOfWater.first().get());
                break;

            case EXITING_RESUPPLY:
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(nearExitDoor);
                if(client.getLocalPlayer().getWorldLocation().equals(nearExitDoor)) {
                    Optional<TileObject> returnDoor = TileObjects.search().withAction("Pass-through").nearestToPlayer();
                    if(returnDoor.isPresent()) {
                        MousePackets.queueClickPacket();
                        TileObjectInteraction.interact(returnDoor.get(), "Pass-through");
                    }
                }

                moonlightGrubs.result().forEach(item -> {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(item, "Drop");
                });

                moonlightGrubPaste.result().forEach(item -> {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(item, "Drop");
                });

                vialsOfWater.result().forEach(item -> {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetAction(item, "Drop");
                });

                break;

            case GOING_TO_NAUGAS:
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(nearNaugas);

                if (sulphurNaugas.nearestToPlayer().isPresent()) {
                    state = State.LOOKING_FOR_TARGET;
                }
                break;

            case LOOKING_FOR_TARGET:

                Optional<NPC> nagua = NPCs.search().withAction("Attack").nameContains("Nagua").noOneInteractingWith().nearestToPlayer();
                if (nagua.isPresent()) {
                    MousePackets.queueClickPacket();
                    NPCInteraction.interact(nagua.get(), "Attack");
                    state = State.FIGHTING;
                }

                break;

            case FIGHTING:
                if(!client.getLocalPlayer().isInteracting()) {
                    state = State.LOOKING_FOR_TARGET;
                }
                break;
        }

    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event)
    {
        if (event.getActor() == client.getLocalPlayer())
        {
            // You just took damage — probably in combat
            state = State.FIGHTING;
        }
    }



    @Subscribe
    public void onGameTick(GameTick event) {
        if(client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        refreshValues();

        if(client.getBoostedSkillLevel(Skill.PRAYER) < 20) {
            if (moonlightPotions.first().isPresent()) {
                Widget potion = moonlightPotions.first().get();
                MousePackets.queueClickPacket();
                InventoryInteraction.useItem(potion, "Drink");
            }
        }

        if(client.getLocalPlayer().getWorldLocation().getRegionID() == 5525 && client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MELEE) == 0) {
            PrayerUtil.togglePrayer(Prayer.PROTECT_FROM_MELEE);
        } else if (client.getLocalPlayer().getWorldLocation().getRegionID() != 5525 && client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MELEE) == 1){
            PrayerUtil.togglePrayer(Prayer.PROTECT_FROM_MELEE);
        }

        evaluateState();
        executeState();


        if (state != previousState) {
            previousState = state;
            ticksSinceLastStateChange = 0;
        } else {
            ticksSinceLastStateChange++;
        }

        if (ticksSinceLastStateChange >= 300) {
            System.out.println("Timeout reached. Logging out...");
            state = State.STUCK_ERROR;
            EthanApiPlugin.stopPlugin(this);
        }


    }
}



