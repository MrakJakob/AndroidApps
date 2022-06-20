package si.uni_lj.fri.pbd.miniapp3.rest

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import si.uni_lj.fri.pbd.miniapp3.Constants
import timber.log.Timber
import java.util.*


object ServiceGenerator {
    private var sBuilder: Retrofit.Builder? = null
    private var sHttpClient: OkHttpClient.Builder? = null
    private var sRetrofit: Retrofit? = null
    private fun init() {
        sHttpClient = OkHttpClient.Builder()
        sBuilder = Retrofit.Builder().baseUrl(Constants.BASE_URL).addConverterFactory(GsonConverterFactory.create()) // TODO: add converter

        /// create Interceptor and add it to client, what does this do?
        val interceptor = HttpLoggingInterceptor()
        sHttpClient!!.addInterceptor(interceptor)   // mybe addNetworkInterceptor

        sRetrofit = sBuilder?.client(sHttpClient?.build())?.build()
        Timber.d("Retrofit built with base url: %s", sRetrofit?.baseUrl()?.toUrl().toString())

    }

    fun <S> createService(serviceClass: Class<S>?): S {
        return sRetrofit!!.create(serviceClass)
    }

    init {
        init()
    }
}