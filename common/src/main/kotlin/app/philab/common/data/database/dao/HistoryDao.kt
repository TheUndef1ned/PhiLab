package app.philab.common.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.philab.common.data.database.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM calc_history ORDER BY created_at DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM calc_history WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM calc_history")
    suspend fun clear()
}
