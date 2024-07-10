package com.example.prophet.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class PatientEntity(
    @PrimaryKey
    val id: Int? = null,
    val name: String,
    val weight: Double,
    val height: Double,
    val age: Int,
    val bmi: Double
) {
    override fun toString(): String {
        return "Patient(id=$id, name='$name', weight=$weight, height=$height, age=$age, bmi=$bmi)"
    }
}