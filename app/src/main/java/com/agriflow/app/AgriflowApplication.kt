/**
 * Represents the class [AgriflowApplication] providing core functionality within the application.
 */
package com.agriflow.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient

@HiltAndroidApp
class AgriflowApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("ngrok-skip-browser-warning", "true")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
