package com.example.roccogotr;

import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;

import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import com.google.inject.Inject;
import java.util.Optional;
import java.util.*;


@PluginDescriptor(name = "RoccoGotr", description = "", enabledByDefault = true, tags = {"Testing"})
public class RoccoGotr extends Plugin {

    @Inject
    Client client;


    State state;

    private static final int DEPOSIT_POOL_ID = 43696;



    @Override
    protected void startUp() throws Exception {

    }



    @Subscribe
    private void onGameTick(GameTick event) {

    }


    private void updateValues() {
        ItemQuery guardianFragments = Inventory.search().withId(ItemID.GUARDIAN_FRAGMENTS);
        ItemQuery pouches = Inventory.search().nameContains("pouch").withAction("Fill");

        Optional<TileObject> rubble = TileObjects.search().nameContains("Rubble").withAction("Climb").nearestToPlayer();

        Optional<TileObject> workbench = TileObjects.search().withId(43754).withAction("Work-at").nearestToPlayer();
        Optional<TileObject> depositPool = TileObjects.search().withId(DEPOSIT_POOL_ID).withAction("Deposit-runes").nearestToPlayer();
        Optional<NPC> guardianNpc = NPCs.search().withAction("Power-up").nameContains("Guardian").first();
        GuardianStatusList guardians = GuardianStatus
                .getActiveGuardians()
                .filterByLevel(client.getRealSkillLevel(Skill.RUNECRAFT));
    }

    private void evaluateState() {
        switch(state) {

        }

    }

    private void executeState() {

    }

}