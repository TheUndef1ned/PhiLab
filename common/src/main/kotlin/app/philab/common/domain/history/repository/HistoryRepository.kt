package app.philab.common.domain.history.repository

import app.philab.common.domain.history.model.CalcRecord
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    fun observeAll(): Flow<List<CalcRecord>>

    suspend fun add(record: CalcRecord): Result<Unit>

    suspend fun delete(id: String): Result<Unit>

    suspend fun clear(): Result<Unit>
}
