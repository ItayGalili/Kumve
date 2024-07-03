package com.example.mykumve.data.db.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mykumve.data.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): LiveData<List<User>>?

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): LiveData<User?>

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): LiveData<User?>
    @Query("SELECT * FROM users WHERE phone = :phone")
    fun getUserByPhone(phone: String): LiveData<User?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUser(user: User): Long

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(vararg user: User)
}
