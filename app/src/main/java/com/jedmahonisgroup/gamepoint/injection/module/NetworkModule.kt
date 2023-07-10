package com.jedmahonisgroup.gamepoint.injection.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.schedulers.Schedulers
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import javax.inject.Inject


/**
 * Module which provides all required dependencies about network
 */
// Safe here as we are dealing with a Dagger 2 module
@Suppress("unused")
@Module
// Safe here as we are dealing with a Dagger 2 module
object NetworkModule {
    /**
     * Provides the Post service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Post service implementation.
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun providePostApi(retrofit: Retrofit): GamePointApi {
        return retrofit.create(GamePointApi::class.java)
    }

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */

    private fun getOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BASIC
        //interceptor.level = HttpLoggingInterceptor.Level.HEADERS

        val builder = OkHttpClient.Builder()
        builder.addInterceptor(interceptor)
//        val gamePointApi = object: GamePointApi
//        builder.authenticator(TokenAuthenticator(gamePointApi))
        //.addInterceptor(SupportInterceptor())

        return builder.build()
    }

    private fun getGson(): Gson {
        return GsonBuilder()
                .setLenient()
                .create()
    }

    data class Error (
            val errors: ArrayList<String>
    )

    @Provides
    @Reusable
    @JvmStatic
    internal fun provideRetrofitInterface(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(getOkHttpClient())
                .build()
    }

//    class TokenAuthenticator @Inject constructor(private val gamePointApi: GamePointApi) : Authenticator {
//
//        override fun authenticate(route: Route, response: Response): Request? {
//            gamePointApi.refreshToken()
//            val newAccessToken = gamePointApi.refreshToken(accessTokenWrapper.getAccessToken()!!.refreshToken).blockingGet()
//            accessTokenWrapper.saveAccessToken(newAccessToken) // save new access_token for next called
//            return response.request().newBuilder()
//                    .header("Authorization", newAccessToken.token) // just only need to override "Authorization" header, don't need to override all header since this new request is create base on old request
//                    .build()
//        }
//    }
//
//    class AccessTokenWrapper constructor(private val sharedPrefApi: SharedPrefApi) {
//        private var accessToken: AccessToken? = null
//
//        // get accessToken from cache or from SharePreference
//        fun getAccessToken(): AccessToken? {
//            if (accessToken == null) {
//                accessToken = sharedPrefApi.getObject(SharedPrefApi.ACCESS_TOKEN, AccessToken::class.java)
//            }
//            return accessToken
//        }
//
//        // save accessToken to SharePreference
//        fun saveAccessToken(accessToken: String) {
//            this.accessToken = accessToken
//
//            sharedPrefApi.putObject(SharedPrefApi.ACCESS_TOKEN, accessToken)
//        }
//    }
}