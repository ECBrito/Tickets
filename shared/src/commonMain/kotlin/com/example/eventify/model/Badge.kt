package com.example.eventify.model

enum class Badge(val title: String, val description: String, val iconName: String) {
    ROOKIE("Rookie", "Bought your 1st ticket!", "ticket"),        // Bronze
    SOCIAL("Social", "Posted 5+ comments", "comment"),            // Prata
    VIP("VIP", "Bought 5+ tickets", "crown")                      // Ouro
}