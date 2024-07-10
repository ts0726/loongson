package com.example.prophet.sqlite

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [PatientEntity::class], version = 2, exportSchema = false)
abstract class PatientDB : RoomDatabase() {

    companion object{
        private var db: PatientDB? = null
        private val name = "patients"

        fun getDB(context: Context) = if (db == null) {
            Log.d("PatientDB", "Creating new instance of DB")
            Room.databaseBuilder(context, PatientDB::class.java, name)
                .fallbackToDestructiveMigration()
                .build().apply {
                db = this
            }
        }else {
            Log.d("PatientDB", "Using existing instance of DB")
            db!!
        }

    }

    abstract fun patientDao(): PatientDao
}