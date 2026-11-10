package app.philab

import android.app.Application
import app.philab.di.AppComponent
import app.philab.di.DaggerAppComponent
import timber.log.Timber

class PhiLabApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }
}
