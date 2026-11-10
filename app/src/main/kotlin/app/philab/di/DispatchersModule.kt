package app.philab.di

import app.philab.common.di.qualifier.IoDispatcher
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
object DispatchersModule {
    @[Provides IoDispatcher]
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
