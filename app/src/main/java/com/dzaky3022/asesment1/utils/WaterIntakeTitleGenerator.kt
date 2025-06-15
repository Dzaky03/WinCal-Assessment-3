package com.dzaky3022.asesment1.utils

class WaterIntakeTitleGenerator {
    
    private val waterWords = listOf(
        "Hydro", "Aqua", "Flow", "Splash", "Drop", "Wave", "Stream", "Ripple",
        "Gulp", "Sip", "Drip", "Pour", "Liquid", "H2O", "Blue", "Clear"
    )
    
    private val lazyWords = listOf(
        "Lazy", "Chill", "Easy", "Simple", "Auto", "Smart", "Quick", "Tap",
        "Effortless", "Smooth", "Casual", "Basic", "Mini", "Pocket", "Snap"
    )
    
    private val actionWords = listOf(
        "Track", "Log", "Count", "Monitor", "Check", "Watch", "Record", "Note",
        "Ping", "Tap", "Mark", "Save", "Store", "Remember", "Catch"
    )
    
    private val timeWords = listOf(
        "Daily", "Today", "Now", "24/7", "Hourly", "Morning", "Evening", "Night",
        "Always", "Instant", "Live", "Real-time", "Non-stop", "Ongoing"
    )
    
    private val cuteWords = listOf(
        "Buddy", "Pal", "Mate", "Friend", "Helper", "Guru", "Coach", "Guide",
        "Keeper", "Tracker", "Ninja", "Master", "Wizard", "Pro", "Hero"
    )
    
    private val suffixes = listOf(
        "ly", "r", "io", "fy", "ze", "wise", "hub", "lab", "zone", "spot",
        "co", "go", "app", "tech", "sync", "flow", "wave", "drop"
    )
    
    // Different title generation patterns
    fun generateTitle(): String {
        return when ((1..8).random()) {
            1 -> "${waterWords.random()}${lazyWords.random()}" // HydroLazy
            2 -> "${lazyWords.random()} ${waterWords.random()} ${actionWords.random()}" // Lazy Drop Track
            3 -> "${waterWords.random()}${suffixes.random()}" // Flowly, Aquafy
            4 -> "${timeWords.random()} ${waterWords.random()}" // Daily Splash
            5 -> "${waterWords.random()} ${cuteWords.random()}" // Hydro Buddy
            6 -> "${lazyWords.random()}${waterWords.random()}${suffixes.random()}" // EasyFlowly
            7 -> "${actionWords.random()} ${waterWords.random()} ${timeWords.random()}" // Track Aqua Daily
            else -> "${cuteWords.random()} ${waterWords.random()}" // Water Ninja
        }
    }
    
    // Generate creative phrases/sentences
    fun generatePhrase(): String {
        val phrases = listOf(
            "${lazyWords.random()} ${timeWords.random().lowercase()} ${waterWords.random().lowercase()}",
            "${waterWords.random().lowercase()} ${actionWords.random().lowercase()} for ${lazyWords.random().lowercase()} people",
            "${timeWords.random().lowercase()} ${waterWords.random().lowercase()} ${actionWords.random().lowercase()}",
            "just ${actionWords.random().lowercase()} your ${waterWords.random().lowercase()}",
            "${waterWords.random().lowercase()} ${cuteWords.random().lowercase()} ${timeWords.random().lowercase()}",
            "${lazyWords.random().lowercase()} ${waterWords.random().lowercase()} ${cuteWords.random().lowercase()}",
            "${actionWords.random().lowercase()} ${waterWords.random().lowercase()}, stay ${lazyWords.random().lowercase()}",
            "your ${timeWords.random().lowercase()} ${waterWords.random().lowercase()} ${cuteWords.random().lowercase()}",
            "${lazyWords.random().lowercase()} ${waterWords.random().lowercase()} ${actionWords.random().lowercase()} ${timeWords.random().lowercase()}",
            "${waterWords.random().lowercase()} ${actionWords.random().lowercase()} made ${lazyWords.random().lowercase()}"
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