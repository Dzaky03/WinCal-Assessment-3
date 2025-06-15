package com.dzaky3022.asesment1.utils

class WaterIntakeTitleGenerator {

    private val drinkWords = listOf(
        "Sip", "Gulp", "Drink", "Hydrate", "Chug", "Pour", "Swig", "Refresh"
    )

    private val timeWords = listOf(
        "Morning", "Noon", "Evening", "Night", "Bedtime", "Late Night", "Sunrise", "Sunset", "Daily", "Today", "Now"
    )

    private val trackingWords = listOf(
        "Log", "Count", "Tracker", "Journal", "Monitor", "Record", "Reminder", "Flow"
    )

    // Different title generation patterns
    fun generateTitle(): String {
        return when ((1..8).random()) {
            1 -> "${drinkWords.random()}${trackingWords.random()}" // HydrateTracker
            2 -> "${timeWords.random()} ${drinkWords.random()}" // Daily Hydrate
            3 -> "${trackingWords.random()} ${drinkWords.random()}" // Log Sip
            4 -> "${timeWords.random()} ${trackingWords.random()}" // Daily Tracker
            5 -> "${drinkWords.random()} ${timeWords.random()}" // Hydrate Today
            6 -> "${trackingWords.random()}${timeWords.random()}" // LogDaily
            7 -> "${drinkWords.random()} ${trackingWords.random()} ${timeWords.random()}" // Drink Log Daily
            else -> "${timeWords.random()} ${drinkWords.random()} ${trackingWords.random()}" // Morning Sip Tracker
        }
    }

    // Generate creative phrases/sentences
    fun generatePhrase(): String {
        val phrases = listOf(
            "${timeWords.random().lowercase()} ${drinkWords.random().lowercase()} ${trackingWords.random().lowercase()}",
            "${drinkWords.random().lowercase()} ${trackingWords.random().lowercase()} for ${timeWords.random().lowercase()}",
            "${timeWords.random().lowercase()} ${trackingWords.random().lowercase()}",
            "just ${trackingWords.random().lowercase()} your ${drinkWords.random().lowercase()}",
            "${drinkWords.random().lowercase()} ${trackingWords.random().lowercase()} ${timeWords.random().lowercase()}",
            "${timeWords.random().lowercase()} ${drinkWords.random().lowercase()} ${trackingWords.random().lowercase()}",
            "${trackingWords.random().lowercase()} your ${drinkWords.random().lowercase()}, ${timeWords.random().lowercase()}",
            "your ${timeWords.random().lowercase()} ${drinkWords.random().lowercase()} ${trackingWords.random().lowercase()}",
            "${timeWords.random().lowercase()} ${drinkWords.random().lowercase()} ${trackingWords.random().lowercase()}",
            "${drinkWords.random().lowercase()} ${trackingWords.random().lowercase()} made simple"
        )
        return phrases.random()
    }

    // Generate multiple suggestions
    fun generateTitleSuggestions(count: Int = 10): List<String> {
        return (1..count).map { generateTitle() }.distinct()
    }

    fun generatePhraseSuggestions(count: Int = 10): List<String> {
        return (1..count).map { generatePhrase() }.distinct()
    }

    // Generate mixed suggestions (titles + phrases)
    fun generateMixedSuggestions(count: Int = 15): List<String> {
        val titles = generateTitleSuggestions(count / 2)
        val phrases = generatePhraseSuggestions(count / 2)
        return (titles + phrases).shuffled().take(count)
    }
}