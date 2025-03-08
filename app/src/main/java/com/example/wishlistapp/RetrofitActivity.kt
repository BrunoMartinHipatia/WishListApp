package com.example.wishlistapp.ui.theme

import com.example.wishlistapp.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitActivity {

    private var retrofit: Retrofit? = null
    private var gson: Gson? = null

    val retrofitInstance: Retrofit?
        get() {
            if (retrofit == null) {
                val token: String = BuildConfig.STEAM_API_KEY
                val client = OkHttpClient.Builder()
              //      .addInterceptor(AuthInterceptor(token))
                    .build()

                gson = GsonBuilder().setLenient().create()

                retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.STEAM_API_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit
        }
}