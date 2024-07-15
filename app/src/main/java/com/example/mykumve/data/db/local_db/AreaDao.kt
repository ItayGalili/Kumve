package com.example.mykumve.data.db.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mykumve.data.model.Area
import com.example.mykumve.data.model.SubArea
import com.example.mykumve.data.model.Trip

import com.example.mykumve.util.DifficultyLevel
@Dao
interface AreaDao {
    @Query("SELECT * FROM areas")
    fun getAllAreas(): List<Area>

    @Query("SELECT * FROM sub_areas WHERE area_id = :areaId")
    fun getSubAreasByAreaId(areaId: Int): List<SubArea>
}
