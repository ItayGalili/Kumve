package com.example.mykumve.data.repository

import com.example.mykumve.data.db.local_db.UserDao
import com.example.mykumve.data.db.repository.UserRepository
import org.junit.Before

class UserRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
//        userDao = mock(UserDao::class.java)
//        userRepository = UserRepository(userDao)
    }

//    @Test
//    fun getUserByEmail_existingUser_returnsUser() = runBlocking {
//        val email = "test@example.com"
//        val user = User("Test", "User", email, null, null, "hashed", "salt")
//        `when`(userDao.getUserByEmail(email)).thenReturn(user)
//
//        val result = userRepository.getUserByEmail(email)
//        assertEquals(user, result)
//    }
//
//    @Test
//    fun insertUser_insertsUser() = runBlocking {
//        val user = User("New", "User", "new@example.com", null, null, "hashed", "salt")
//
//        userRepository.insertUser(user)
//        verify(userDao).insertUser(user)
//    }
}
