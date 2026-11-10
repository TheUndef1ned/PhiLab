package app.philab.di

import app.philab.common.data.repository.impl.AssetPhiTableRepository
import app.philab.common.data.repository.impl.RoomHistoryRepository
import app.philab.common.domain.history.repository.HistoryRepository
import app.philab.common.domain.phi.repository.PhiTableRepository
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {

    @Binds
    fun bindHistoryRepository(impl: RoomHistoryRepository): HistoryRepository

    @Binds
    fun bindPhiTableRepository(impl: AssetPhiTableRepository): PhiTableRepository
}
