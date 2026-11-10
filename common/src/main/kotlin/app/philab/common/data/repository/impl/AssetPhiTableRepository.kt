package app.philab.common.data.repository.impl

import app.philab.common.data.datasource.PhiTableAssetDataSource
import app.philab.common.di.annotations.AppScope
import app.philab.common.domain.phi.model.PhiTable
import app.philab.common.domain.phi.repository.PhiTableRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/** Parses the asset once and keeps the table in memory (500 doubles — O(1) access by index). */
@AppScope
class AssetPhiTableRepository @Inject constructor(
    private val dataSource: PhiTableAssetDataSource,
) : PhiTableRepository {

    private val mutex = Mutex()

    @Volatile
    private var cached: PhiTable? = null

    override suspend fun getTable(): Result<PhiTable> = runCatching {
        cached ?: mutex.withLock {
            cached ?: dataSource.load().also { cached = it }
        }
    }
}
