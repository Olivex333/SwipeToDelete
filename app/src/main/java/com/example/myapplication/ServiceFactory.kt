package com.example.myapplication

import com.example.myapplication.BuildConfig
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceFactory {
    fun createUserService(): UserService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(createSecureOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(UserService::class.java)
    }

    internal fun createSecureOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .certificatePinner(
                CertificatePinner.Builder()
                    .add("jsonplaceholder.typicode.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                    .build()
            )
            .build()
    }
}
