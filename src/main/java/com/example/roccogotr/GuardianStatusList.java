package com.example.roccogotr;

import java.util.List;
import java.util.Iterator;
import java.util.stream.Collectors;


public class GuardianStatusList {
    private final List<GuardianStatus> guardians;

    public GuardianStatusList(List<GuardianStatus> guardians) {
        this.guardians = guardians;
    }

    public GuardianStatusList filterByLevel(int level) {
        List<GuardianStatus> filtered = guardians.stream()
                .filter(g -> g.levelRequired <= level)
                .collect(Collectors.toList());
        return new GuardianStatusList(filtered);
    }

    public List<GuardianStatus> toList() {
        return guardians;
    }

    // Optionally delegate other List methods for convenience
    public Iterator<GuardianStatus> iterator() {
        return guardians.iterator();
    }

}