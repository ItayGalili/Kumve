package com.example.mykumve.data.db

/**
 * Singleton pattern for providing the Room database instance.
 *
 * TODO: Ensure thread-safety and handle migrations if necessary.
 */
object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
            }
        }
        return INSTANCE!!
    }
}
