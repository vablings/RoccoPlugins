package com.example.roccogotr;

import com.google.common.collect.ImmutableSet;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Utility.WorldAreaUtility;
import com.google.inject.Inject;
import net.runelite.api.*;
import java.util.*;
import java.util.stream.Collectors;

public enum GuardianStatus {
    AIR("Air", 43701, 1, RuneAlignment.ELEMENTAL),
    WATER("Water", 43702, 5, RuneAlignment.ELEMENTAL),
    EARTH("Earth", 43703, 9, RuneAlignment.ELEMENTAL),
    FIRE("Fire", 43704, 14, RuneAlignment.ELEMENTAL),
    MIND("Mind", 43705, 2, RuneAlignment.CATALYTIC),
    BODY("Body", 43709, 20, RuneAlignment.CATALYTIC),
    COSMIC("Cosmic", 43710, 27, RuneAlignment.CATALYTIC),
    CHAOS("Chaos", 43706, 35, RuneAlignment.CATALYTIC),
    NATURE("Nature", 43711, 44, RuneAlignment.CATALYTIC),
    LAW("Law", 43712, 54, RuneAlignment.CATALYTIC),
    DEATH("Death", 43707, 65, RuneAlignment.CATALYTIC),
    BLOOD("Blood", 43708, 77, RuneAlignment.CATALYTIC);

    public enum RuneAlignment {
        ELEMENTAL, CATALYTIC
    }

    @Inject
    Client client;


    public final String name;
    public final int objectID;
    public final int levelRequired;
    public final RuneAlignment type;

    GuardianStatus(String name, int objectID, int levelRequired, RuneAlignment type) {
        this.name = name;
        this.objectID = objectID;
        this.levelRequired = levelRequired;
        this.type = type;
    }

    public static GuardianStatusList getActiveGuardians() {
        List<GuardianStatus> active = Arrays.stream(GuardianStatus.values())
                .filter(guardian -> isActive(guardian.objectID))
                .collect(Collectors.toList());
        return new GuardianStatusList(active);
    }

    private static boolean isActive(int objectID) {
        Optional<TileObject> tileObject = TileObjects.search().withId(objectID).first();
        return tileObject.map(GuardianStatus::checkIfActive).orElse(false);
    }

    private static boolean checkIfActive(TileObject guardian) {
        if(guardian instanceof GameObject) {
            GameObject gameObject = (GameObject) guardian;
            if (gameObject.getRenderable() instanceof DynamicObject) {
                Animation animation = ((DynamicObject) gameObject.getRenderable()).getAnimation();
                int ACTIVE_ANIMATION_ID = 9363;
                return animation.getId() == ACTIVE_ANIMATION_ID;
            }
        }
        return false;
    }


}