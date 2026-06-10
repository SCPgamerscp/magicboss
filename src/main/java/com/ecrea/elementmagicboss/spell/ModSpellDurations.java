package com.ecrea.elementmagicboss.spell;

public final class ModSpellDurations {
    private ModSpellDurations() {
    }

    public static int scalingDurationTicks(int spellLevel, float spellPower) {
        return Math.round((spellLevel * 10.0f + spellPower) * 20.0f);
    }
}
