package com.mvc.simple.repository

import android.content.Context
import com.mvc.simple.repository.ApiService.Companion.createRetrofit
import com.mvc.simple.util.SharedPreference
import org.koin.core.KoinComponent

class RepoModel(context: Context) : KoinComponent {

    val appPreference = SharedPreference(context)
    var api = createRetrofit(appPreference).create(ApiService::class.java)
}