package com.mvc.simple

import android.app.Application
import android.content.Context
import com.mvc.simple.repository.RepoModel
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

open class MyApplication : Application() {

    var _appContext: Context? = null

    override fun onCreate() {
        super.onCreate()
        _appContext = this
        context = applicationContext

        val myModules = module {
            single { RepoModel(this@MyApplication) }
        }

        startKoin {
            androidLogger()
            modules(myModules)
        }
    }

    companion object {
        lateinit var context: Context
    }


}