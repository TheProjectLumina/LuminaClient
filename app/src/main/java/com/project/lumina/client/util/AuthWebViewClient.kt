package com.project.lumina.client.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.JsonParser
import com.project.lumina.client.constructors.AccountManager
import com.project.lumina.client.model.Account
import com.project.lumina.relay.util.XboxDeviceInfo
import com.project.lumina.relay.util.XboxGamerTagException
import com.project.lumina.relay.util.base64Decode
import com.project.lumina.relay.util.fetchIdentityToken
import com.project.lumina.relay.util.fetchRawChain
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.random.nextInt

@SuppressLint("SetJavaScriptEnabled")
class AuthWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {

    private var data: String? = null

    private var account: Pair<String, String>? = null

    private val handler = Handler(Looper.getMainLooper())

    var deviceInfo: XboxDeviceInfo? = null

    var callback: ((success: Boolean) -> Unit)? = null

    init {
        CookieManager.getInstance()
            .removeAllCookies(null)

        settings.javaScriptEnabled = true
        webViewClient = AuthWebViewClient()
    }

    fun addAccount() {
        loadUrl("https://login.live.com/oauth20_authorize.srf?client_id=${deviceInfo!!.appId}&redirect_uri=https://login.live.com/oauth20_desktop.srf&response_type=code&scope=service::user.auth.xboxlive.com::MBI_SSL")
    }

    inner class AuthWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            if (account != null && (request.url.scheme ?: "").startsWith("ms-xal")) {
                thread {
                    try {
                        handler.post { showLoadingPage("Verifying your credentials...") }

                        val identityToken = fetchIdentityToken(account!!.first, deviceInfo!!)
                        handler.post { showLoadingPage("Almost done...") }
                        val username = getUsernameFromChain(
                            fetchRawChain(
                                identityToken.token,
                                EncryptionUtils.createKeyPair().public
                            ).readText()
                        )

                        
                        val newAccount = Account(
                            username,
                            deviceInfo!!,
                            account!!.second
                        )
                        AccountManager.accounts.add(newAccount)
                        AccountManager.save()
                        
                        
                        AccountManager.selectAccount(newAccount)

                        callback?.invoke(true)
                    } catch (t: Throwable) {
                        Log.e("AuthWebView", "Obtain access token: ${t.stackTraceToString()}")
                        handler.post { loadData(t.stackTraceToString()) }
                    }
                }
                return true
            }
            val url = request.url.toString().toHttpUrlOrNull() ?: return false
            if (url.host != "login.live.com" || url.encodedPath != "/oauth20_desktop.srf") {
                if (url.queryParameter("res") == "cancel") {
                    Log.e("AuthWebView", "Action cancelled")
                    callback?.invoke(false)
                    return false
                }
                Log.e("AuthWebView", "Invalid url ${request.url}")
                return false
            }

            val authCode = url.queryParameter("code") ?: return false


            showLoadingPage("Setting up your account...")
            thread {
                try {
                    val (accessToken, refreshToken) = deviceInfo!!.refreshToken(authCode)
                    handler.post { showLoadingPage("Authenticating with Xbox...") }

                    val username = try {
                        val identityToken = fetchIdentityToken(accessToken, deviceInfo!!)
                        handler.post { showLoadingPage("Retrieving your profile...") }
                        getUsernameFromChain(
                            fetchRawChain(
                                identityToken.token,
                                EncryptionUtils.createKeyPair().public
                            ).readText()
                        )
                    } catch (e: XboxGamerTagException) {
                        account = accessToken to refreshToken
                        handler.post {
                            loadUrl(e.sisuStartUrl)
                        }
                        return@thread
                    }

                    
                    val account = Account(username, deviceInfo!!, refreshToken)
                    while (AccountManager.accounts.map { it.remark }.contains(account.remark)) {
                        account.remark += Random.nextInt(0..9)
                    }
                    AccountManager.accounts.add(account)
                    AccountManager.save()
                    
                    
                    
                    AccountManager.selectAccount(account)

                    callback?.invoke(true)
                } catch (t: Throwable) {
                    
                    Log.e("AuthWebView", "Obtain access token: ${t.stackTraceToString()}")
                    handler.post { loadData(t.stackTraceToString()) }
                }
            }
            return true
        }

    }

    private fun getUsernameFromChain(chains: String): String {
        val body = JsonParser.parseString(chains).asJsonObject.getAsJsonArray("chain")
        for (chain in body) {
            val chainBody =
                JsonParser.parseString(base64Decode(chain.asString.split(".")[1]).toString(Charsets.UTF_8)).asJsonObject
            if (chainBody.has("extraData")) {
                val extraData = chainBody.getAsJsonObject("extraData")
                return extraData.get("displayName").asString
            }
        }
        error("no username found")
    }

    fun showLoadingPage(title: String) {
        val data = this.data ?: context.assets.open("loading.html").readBytes().decodeToString()
        val replacedData = data.replace("\$title", title)
        val encodedText = Base64.encodeToString(replacedData.toByteArray(), Base64.DEFAULT)
        loadData(encodedText, "text/html; charset=UTF-8", "base64")
    }

    fun loadData(text: String) {
        loadData(text, "text/html", "UTF-8")
    }

}
