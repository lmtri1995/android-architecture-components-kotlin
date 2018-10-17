package net.snaglobal.trile.wizeye.data.remote.login

import android.util.Log
import net.snaglobal.trile.wizeye.data.remote.RemoteContract
import net.snaglobal.trile.wizeye.data.remote.RetrofitClient
import net.snaglobal.trile.wizeye.data.remote.model.LoginCredential
import net.snaglobal.trile.wizeye.data.remote.model.LoginResponse
import java.io.IOException

/**
 * @author lmtri
 * @since Oct 15, 2018 at 3:00 PM
 */
object LoginClient {
    @JvmStatic
    @Throws(IOException::class)
    fun login(domain: String, loginCredential: LoginCredential): LoginResponse? {
        val httpUrl = RemoteContract.WIZEYE_LOGIN_URL.format(domain)

        val loginClient = RetrofitClient.getInstance(httpUrl)
                .create(ILoginClient::class.java)

        val call = loginClient.login(loginCredential)

        val response = call.execute()

        if (response.isSuccessful) {
            response.body()?.run {
                Log.d("API", "successfulStatus: $successfulStatus")
                Log.d("API", "message: $message")
                Log.d("API", "token: $token")
                return this
            }
        }
        return null
    }
}