package com.example.mykumve.data.db.local_db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mykumve.data.model.User

/**
 * DAO interface for user-related database operations.
 * Defines methods for querying and inserting users.
 *
 * TODO: Add methods for updating and deleting users if necessary.
 */
@Dao
interface UserDao {
//    @Query("SELECT * FROM users ORDER BY id DESC")
//    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUser(user: User): Long

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(vararg user: User)
}
