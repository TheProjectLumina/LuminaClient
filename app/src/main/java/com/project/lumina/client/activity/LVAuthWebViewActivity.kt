package com.project.lumina.client.activity

import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.project.lumina.client.util.SessionManager

class LVAuthWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTH_URL = "auth_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authUrl = intent.getStringExtra(EXTRA_AUTH_URL) ?: ""

        if (authUrl.isEmpty()) {
            finish()
            return
        }

        val webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.contains("projectlumina.online")) {
                    val sessionManager = SessionManager(applicationContext)
                    sessionManager.saveSession()
                    val packageManager = applicationContext.packageManager
                    val intent = packageManager.getLaunchIntentForPackage(applicationContext.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                    return true
                }

                return false
            }
        }

        webView.loadUrl(authUrl)
    }
}