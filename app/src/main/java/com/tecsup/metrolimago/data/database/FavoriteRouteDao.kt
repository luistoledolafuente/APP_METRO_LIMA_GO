package com.tecsup.metrolimago.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface FavoriteRouteDao {
    @Query("SELECT * FROM favorite_routes")
    suspend fun getAll(): List<FavoriteRouteEntity>

    @Insert
    suspend fun insert(route: FavoriteRouteEntity)

    @Delete
    suspend fun delete(route: FavoriteRouteEntity)
}
