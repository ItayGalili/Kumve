package com.example.mykumve.util

import com.example.mykumve.R

enum class DifficultyLevel(val id: Int, val stringResId: Int) {
    UNSET(0, R.string.difficulty_unset),
    EASY(1, R.string.difficulty_easy),
    MEDIUM(2, R.string.difficulty_medium),
    HARD(3, R.string.difficulty_hard);

    fun prettyString() = stringResId

    companion object {
        fun fromId(id: Int): DifficultyLevel {
            return values().find { it.id == id } ?: UNSET
        }
    }
}

data class Equipment(
    val name: String,
    val done: Boolean,
    val userId: Int
)