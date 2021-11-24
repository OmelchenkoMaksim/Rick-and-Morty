package com.android.andersenrickandmorty.retrofit

import com.android.andersenrickandmorty.common.DataBase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    fun retrofitGetInstance(): RetrofitApi {
/*          use for logging retro response
            val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()*/

        return Retrofit.Builder()
            .baseUrl(DataBase.BASE_URL)
            // .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitApi::class.java)
    }
}