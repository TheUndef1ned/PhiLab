package app.philab.di

import android.app.Application
import app.philab.MainActivity
import app.philab.common.di.annotations.AppScope
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(
    modules = [
        ApplicationModule::class,
        DatabaseModule::class,
        RepositoryModule::class,
        StoreModule::class,
        DispatchersModule::class,
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(activity: MainActivity)
}
