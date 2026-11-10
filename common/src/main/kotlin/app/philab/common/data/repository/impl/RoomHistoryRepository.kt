package app.philab.common.data.repository.impl

import app.philab.common.data.database.dao.HistoryDao
import app.philab.common.data.repository.mapper.toDomain
import app.philab.common.data.repository.mapper.toEntity
import app.philab.common.di.annotations.AppScope
import app.philab.common.domain.history.model.CalcRecord
import app.philab.common.domain.history.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AppScope
class RoomHistoryRepository @Inject constructor(
    private val dao: HistoryDao,
) : HistoryRepository {

    override fun observeAll(): Flow<List<CalcRecord>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun add(record: CalcRecord): Result<Unit> = runCatching { dao.insert(record.toEntity()) }

    override suspend fun delete(id: String): Result<Unit> = runCatching { dao.delete(id) }

    override suspend fun clear(): Result<Unit> = runCatching { dao.clear() }
}
