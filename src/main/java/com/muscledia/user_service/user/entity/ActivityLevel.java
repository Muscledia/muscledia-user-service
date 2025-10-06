package com.muscledia.user_service.user.entity;

/**
 * Represents different levels of physical activity for calorie calculation.
 * Each level has a corresponding activity multiplier used in TDEE (Total Daily Energy Expenditure) calculations.
 */
public enum ActivityLevel {
    /**
     * Sedentary: Little or no exercise (1.2x multiplier)
     */
    SEDENTARY(1.2),

    /**
     * Lightly Active: Light exercise/sports 1-3 days/week (1.375x multiplier)
     */
    LIGHTLY_ACTIVE(1.375),

    /**
     * Moderately Active: Moderate exercise/sports 3-5 days/week (1.55x multiplier)
     */
    MODERATELY_ACTIVE(1.55),

    /**
     * Very Active: Hard exercise/sports 6-7 days a week (1.725x multiplier)
     */
    VERY_ACTIVE(1.725),

    /**
     * Extra Active: Very hard exercise/sports & physical job or training twice per day (1.9x multiplier)
     */
    EXTRA_ACTIVE(1.9);

    private final double multiplier;

    ActivityLevel(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
