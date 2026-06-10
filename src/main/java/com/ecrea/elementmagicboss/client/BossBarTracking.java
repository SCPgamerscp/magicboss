package com.ecrea.elementmagicboss.client;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BossBarTracking {
    private static final Set<UUID> CUSTOM_BOSS_BARS = new HashSet<>();

    public static void startTracking(UUID uuid) {
        CUSTOM_BOSS_BARS.add(uuid);
        System.out.println("BossBarTracking: Added UUID=" + uuid + ", Total tracked: " + CUSTOM_BOSS_BARS.size());
    }

    public static void stopTracking(UUID uuid) {
        CUSTOM_BOSS_BARS.remove(uuid);
        System.out.println("BossBarTracking: Removed UUID=" + uuid + ", Total tracked: " + CUSTOM_BOSS_BARS.size());
    }

    public static boolean isCustom(UUID uuid) {
        return CUSTOM_BOSS_BARS.contains(uuid);
    }
}
