package com.example.roccodriftnet;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum DriftNetStatus
{
    UNSET(Color.YELLOW),
    SET(Color.GREEN),
    CATCH(Color.GREEN),
    FULL(Color.RED);

    private final Color color;

    static DriftNetStatus of(int varbitValue)
    {
        switch (varbitValue)
        {
            case 0:
                return UNSET;
            case 1:
                return SET;
            case 2:
                return CATCH;
            case 3:
                return FULL;
            default:
                return null;
        }
    }
}