package il.co.erg.mykumve.data.db.local_db

import il.co.erg.mykumve.util.Converters
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import il.co.erg.mykumve.data.db.local_db.model.Area
import il.co.erg.mykumve.data.db.local_db.model.SubArea
import il.co.erg.mykumve.data.db.local_db.model.Trip
import il.co.erg.mykumve.data.db.local_db.model.TripInfo
import il.co.erg.mykumve.data.db.local_db.model.TripInvitation
import il.co.erg.mykumve.data.db.local_db.model.User

@Database(
    entities = [User::class, Trip::class, TripInfo::class, TripInvitation::class, Area::class, SubArea::class],
    version = 16)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun tripInfoDao(): TripInfoDao
    abstract fun userDao(): UserDao
    abstract fun tripInvitationDao(): TripInvitationDao
    abstract fun areaDao(): AreaDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "kumve_db")
                    .fallbackToDestructiveMigration()
//                    .allowMainThreadQueries()
                    .build().also { instance = it }
            }
        }
    }
}
