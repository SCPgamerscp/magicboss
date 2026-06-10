package com.ecrea.elementmagicboss.client.util;

public class BossbarStacker {
    private static int currentYOffset = 0;

    /**
     * Resets the Y offset at the beginning of the frame.
     * @param initialOffset Usually the height occupied by vanilla bossbars.
     */
    public static void reset(int initialOffset) {
        currentYOffset = initialOffset;
    }

    /**
     * Gets the current Y offset and increments it by the specified amount.
     * @param height The height occupied by the bossbar being rendered.
     * @return The Y offset to use for rendering.
     */
    public static int getAndIncrement(int height) {
        int y = currentYOffset;
        currentYOffset += height;
        return y;
    }

    /**
     * Gets the current accumulated Y offset.
     */
    public static int getCurrentOffset() {
        return currentYOffset;
    }
}
