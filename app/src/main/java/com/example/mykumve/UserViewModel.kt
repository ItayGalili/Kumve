package com.example.mykumve


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.mykumve.data.db.repository.UserRepository
import com.example.mykumve.data.model.User

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository = UserRepository(application)

    fun getAllUsers(): LiveData<List<User>>? = userRepository.getAllUsers()

    fun deleteUser(user: User) {
        // פעולה למחיקת משתמש מהמאגר
    }

    fun saveSelectedUsers(users: List<User>) {
        // פעולה לשמירת המשתמשים שנבחרו
    }
}