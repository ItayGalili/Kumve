package com.example.mykumve.data.model

/**
 * Data class representing a plant for AI flora identification.
 * Contains plant details such as name, description, and image URL.
 *
 * TODO: Define properties and methods required for AI flora identification.
 */
data class Plant(
    @PrimaryKey(auto-generate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val imageUrl: String
)
