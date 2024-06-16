package com.example.mykumve.data.db

/**
 * Abstract class for Room database setup.
 * Includes DAOs for accessing the User and Trip tables.
 *
 * TODO: Add any additional entities and their DAOs if necessary.
 */
@Database(entities = [User::class, Trip::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
}
