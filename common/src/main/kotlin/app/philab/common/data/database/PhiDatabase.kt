package app.philab.common.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.philab.common.data.database.dao.HistoryDao
import app.philab.common.data.database.entity.HistoryEntity
import app.philab.common.data.database.utils.DatabaseConverters

@Database(
    entities = [
        HistoryEntity::class,
    ],
    version = PhiDatabase.VERSION,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class PhiDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        const val DATABASE_NAME = "philab.db"
        const val VERSION = 1
    }
}

