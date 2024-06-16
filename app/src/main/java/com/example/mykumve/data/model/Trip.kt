package com.example.mykumve.data.model

import androidx.room.PrimaryKey

/**
 * Data class representing a trip.
 * Contains trip details such as name, start date, end date, equipment, participants, and route.
 *
 * TODO: Add methods to handle JSON conversion for equipment, participants, and route.
 */
data class Trip(
    @PrimaryKey( true) val id: Int = 0,
    val name: String,
    val startDate: Long,
    val endDate: Long,
    val equipment: String, // JSON string representing the list of equipment
    val participants: String, // JSON string representing the list of participants
    val route: String // JSON string representing the route details
)
