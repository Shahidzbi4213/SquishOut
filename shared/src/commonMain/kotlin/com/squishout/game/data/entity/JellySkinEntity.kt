package com.squishout.game.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jelly_skins")
data class JellySkinEntity(
    @PrimaryKey val jellyId: String,
    val name: String,
    val rarity: String, // COMMON, RARE, MYTHIC, EVENT
    val isUnlocked: Boolean,
    val level: Int,
    val isEquipped: Boolean,
    val perkDescription: String? = null
)
