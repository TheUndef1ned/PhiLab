package app.philab.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dagger.Module
import dagger.Provides

@Module
object StoreModule {
    @Provides
    fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()
}
