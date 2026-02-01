package com.project.lumina.client.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.project.lumina.client.ui.theme.LuminaClientTheme

class DiscordLoginActivity : ComponentActivity() {

    private var webView: WebView? = null
    private var tokenExtracted = false
    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LuminaClientTheme {
                DiscordLoginContent(
                    isLoading = isLoading,
                    onClose = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onWebViewCreated = { view ->
                        webView = view
                        setupWebView(view)
                        view.loadUrl(DISCORD_LOGIN_URL)
                    }
                )
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            if (Build.MANUFACTURER.equals("motorola", ignoreCase = true)) {
                userAgentString = SAMSUNG_USER_AGENT
            }
        }
        webView.webViewClient = DiscordWebViewClient()
        webView.webChromeClient = DiscordWebChromeClient()
    }

    private fun injectTokenExtraction() {
        if (!tokenExtracted) {
            webView?.loadUrl(JS_TOKEN_EXTRACTION)
        }
    }

    private fun onTokenExtracted(token: String?) {
        if (!token.isNullOrEmpty() && token != "undefined") {
            tokenExtracted = true
            val resultIntent = Intent().apply {
                putExtra(EXTRA_TOKEN, token)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            showError("Failed to extract token. Please try again.")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            setResult(Activity.RESULT_CANCELED)
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView?.destroy()
        super.onDestroy()
    }

    private inner class DiscordWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            isLoading = true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            isLoading = false
            if (url?.contains("/app") == true) {
                injectTokenExtraction()
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            if (url.contains("/app")) {
                view?.loadUrl(url)
                return true
            }
            return false
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame == true) {
                isLoading = false
                showError("Network error. Please check your connection.")
            }
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            isLoading = false
            showError("Security error. Cannot proceed.")
            handler?.cancel()
        }
    }

    private inner class DiscordWebChromeClient : WebChromeClient() {
        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            if (!message.isNullOrEmpty()) {
                onTokenExtracted(message)
            }
            result?.confirm()
            return true
        }
    }

    companion object {
        const val EXTRA_TOKEN = "token"
        private const val DISCORD_LOGIN_URL = "https://discord.com/login"
        private const val SAMSUNG_USER_AGENT = "Mozilla/5.0 (Linux; Android 12; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        private const val JS_TOKEN_EXTRACTION =
            "javascript:(function(){" +
            "var i=document.createElement('iframe');" +
            "document.body.appendChild(i);" +
            "alert(i.contentWindow.localStorage.token.slice(1,-1))" +
            "})()"
    }
}

@Composable
private fun DiscordLoginContent(
    isLoading: Boolean,
    onClose: () -> Unit,
    onWebViewCreated: (WebView) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Discord Login",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Sign in to connect your account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    onWebViewCreated(this)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}