package com.jedmahonisgroup.gamepoint.network

import okhttp3.Interceptor
import okhttp3.Response


/**
 * Created by Aleckson Nyamwaya on 06/20/2019
 * Secondary helper interceptor to skip interceptor headers over Data Module
 */
class SupportInterceptor: Interceptor{

    /**
     * Interceptor class for setting of the headers for every request
     */

    val bearerToken: String = "d7vRCyJI_38c9VEJyKyXMZQCa5noW8jx"

    override  fun intercept(chain: Interceptor.Chain): Response{
        var request = chain.request()
        request = request.newBuilder()
            .addHeader("Bearer", bearerToken)
                .build()
        return chain.proceed(request)
    }

//    @Throws(IOException::class)
//    override fun authenticate(route: Route, response: Response): Request? {
//        var requestAvailable: Request? = null
//        return try {
//            requestAvailable = response?.request()?.newBuilder()
//                    ?.addHeader("AUTH_TOKEN", "UUID.randomUUID().toString()")
//                    ?.build()
//            requestAvailable
//        } catch (ex: Exception){
//            requestAvailable
//        }
//    }


}