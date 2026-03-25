package com.example.onestep.util

import java.util.Locale

object StepUtils {
    /**
     * Estimates distance in kilometers from step count.
     * Formula: steps * 0.762 meters / 1000
     */
    fun calculateDistanceKm(steps: Int): Double {
        return (steps * 0.762) / 1000.0
    }

    /**
     * Estimates calories burned from step count.
     * Formula: steps * 0.04 calories
     */
    fun calculateCalories(steps: Int): Double {
        return steps * 0.04
    }
}
