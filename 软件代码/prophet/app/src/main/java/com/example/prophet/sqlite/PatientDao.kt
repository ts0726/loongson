package com.example.prophet.sqlite

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatient(patient: PatientEntity)

    @Delete
    fun deletePatient(patient: PatientEntity)

    @Update
    fun updatePatient(patient: PatientEntity)

    @Query("SELECT * FROM PatientEntity WHERE id = :id")
    fun getPatient(id: Int): PatientEntity

    @Query("SELECT * FROM PatientEntity")
    fun getAllPatients(): List<PatientEntity>
}