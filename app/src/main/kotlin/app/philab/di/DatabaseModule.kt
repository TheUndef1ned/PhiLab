package app.philab.di

import android.content.Context
import androidx.room.Room
import app.philab.common.data.database.PhiDatabase
import app.philab.common.data.database.dao.HistoryDao
import app.philab.common.di.annotations.AppScope
import dagger.Module
import dagger.Provides

@Module
object DatabaseModule {

    @[Provides AppScope]
    fun provideDatabase(context: Context): PhiDatabase =
        Room.databaseBuilder(context, PhiDatabase::class.java, PhiDatabase.DATABASE_NAME)
            .build()

    @[Provides AppScope]
    fun provideHistoryDao(database: PhiDatabase): HistoryDao = database.historyDao()
}
