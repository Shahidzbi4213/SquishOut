package com.squishout.game.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.squishout.game.data.entity.JellySkinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JellySkinDao {
    @Query("SELECT * FROM jelly_skins ORDER BY rarity ASC, name ASC")
    fun getAllSkins(): Flow<List<JellySkinEntity>>

    @Query("SELECT * FROM jelly_skins WHERE isEquipped = 1 LIMIT 1")
    fun getEquippedSkin(): Flow<JellySkinEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(skins: List<JellySkinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(skin: JellySkinEntity)

    @Query("UPDATE jelly_skins SET isEquipped = 0")
    suspend fun clearEquipped()

    @Query("UPDATE jelly_skins SET isEquipped = 1 WHERE jellyId = :jellyId")
    suspend fun markEquipped(jellyId: String)

    @Transaction
    suspend fun equipSkin(jellyId: String) {
        clearEquipped()
        markEquipped(jellyId)
    }
}
