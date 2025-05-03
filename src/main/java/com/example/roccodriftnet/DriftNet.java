package com.example.roccodriftnet;

import java.util.Set;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.annotations.Varbit;
import net.runelite.api.coords.WorldPoint;

@Data
@RequiredArgsConstructor
class DriftNet
{
    private final int objectId;
    @Getter(onMethod_ = {@Varbit})
    private final int statusVarbit;
    @Getter(onMethod_ = {@Varbit})
    private final int countVarbit;
    private final Set<WorldPoint> adjacentTiles;

    private GameObject net;
    private DriftNetStatus status;
    private int count;
    @Setter
    private DriftNetStatus prevTickStatus;

    // Nets that are not accepting fish are those currently not accepting, or those which were not
    // accepting in the previous tick. (When a fish shoal is 2 tiles adjacent to a drift net and is
    // moving to a net that is just being setup it will be denied even though the net is currently
    // in the CATCHING status)
    boolean isNotAcceptingFish()
    {
        return (status != DriftNetStatus.CATCH && status != DriftNetStatus.SET) ||
                (prevTickStatus != DriftNetStatus.CATCH && prevTickStatus != DriftNetStatus.SET);
    }

    String getFormattedCountText()
    {
        return status != DriftNetStatus.UNSET ? count + "/10" : "";
    }
}