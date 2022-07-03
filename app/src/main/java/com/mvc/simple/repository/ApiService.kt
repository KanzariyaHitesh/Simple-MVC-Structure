package com.mvc.simple.repository

import com.mvc.simple.model.EventListApiResponse
import com.mvc.simple.util.Constants
import com.mvc.simple.util.SharedPreference
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

public interface ApiService {

    @JvmSuppressWildcards
    @FormUrlEncoded
    @POST("api.php")
    fun callGetEventListApi(
        @FieldMap params: Map<String, Any?>
    ): Observable<Response<EventListApiResponse>>

    companion object {
        lateinit var retrofit: Retrofit
        private val timeout = 30

        fun createRetrofit(appPreference: SharedPreference): Retrofit {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(createOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            return retrofit
        }

        private fun createOkHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
            builder.readTimeout(timeout.toLong(), TimeUnit.SECONDS)
            builder.writeTimeout(timeout.toLong(), TimeUnit.SECONDS)
            builder.connectTimeout(timeout.toLong(), TimeUnit.SECONDS)

            builder.addInterceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url
                val url = originalUrl.newBuilder()
                    .build()
                val requestBuilder = originalRequest.newBuilder()
                    .url(url)
                chain.proceed(requestBuilder.build())
            }
            return builder.build()
        }
    }

}

