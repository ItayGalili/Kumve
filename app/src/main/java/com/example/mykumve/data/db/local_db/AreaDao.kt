package com.example.mykumve.data.db.local_db

import androidx.room.Dao
import androidx.room.Query
import com.example.mykumve.data.model.Area
import com.example.mykumve.data.model.SubArea

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas")
    fun getAllAreas(): List<Area>

    @Query("SELECT * FROM sub_areas WHERE area_id = :areaId")
    fun getSubAreasByAreaId(areaId: Int): List<SubArea>
}
