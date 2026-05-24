package com.snippyseat.app.data.network

import com.snippyseat.app.core.connectivity.ConnectivityObserver
import com.snippyseat.app.core.connectivity.NoInternetException
import com.snippyseat.app.data.auth.TokenManager
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class TooManyRequestsException : IOException("Too many requests")
class BlockedAccountException : IOException("Account is blocked")
class ServerUnavailableException : IOException("Server unavailable")

@Singleton
class NoInternetInterceptor @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val online = runBlocking { connectivityObserver.isOnline.first() }
        if (!online) throw NoInternetException()
        return chain.proceed(chain.request())
    }
}

@Singleton
class HttpErrorInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        when (response.code) {
            401 -> {
                runBlocking { tokenManager.clear() }
                return response
            }
            403 -> {
                val peek = response.peekBody(512).string()
                if (peek.contains("blocked", ignoreCase = true)) {
                    response.close()
                    throw BlockedAccountException()
                }
            }
            429 -> {
                response.close()
                throw TooManyRequestsException()
            }
            in 500..599 -> {
                response.close()
                throw ServerUnavailableException()
            }
        }
        return response
    }
}

