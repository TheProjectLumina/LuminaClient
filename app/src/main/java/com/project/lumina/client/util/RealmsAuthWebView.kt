/**
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

/*
 * ██████╗ ███████╗ █████╗ ██╗     ███╗   ███╗███████╗ █████╗  ██████╗████████╗██╗██╗   ██╗██╗████████╗██╗   ██╗
 * ██╔══██╗██╔════╝██╔══██╗██║     ████╗ ████║██╔════╝██╔══██╗██╔════╝╚══██╔══╝██║██║   ██║██║╚══██╔══╝╚██╗ ██╔╝
 * ██████╔╝█████╗  ███████║██║     ██╔████╔██║███████╗███████║██║        ██║   ██║██║   ██║██║   ██║    ╚████╔╝
 * ██╔══██╗██╔══╝  ██╔══██║██║     ██║╚██╔╝██║╚════██║██╔══██║██║        ██║   ██║╚██╗ ██╔╝██║   ██║     ╚██╔╝
 * ██║  ██║███████╗██║  ██║███████╗██║ ╚═╝ ██║███████║██║  ██║╚██████╗   ██║   ██║ ╚████╔╝ ██║   ██║      ██║
 * ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝╚══════╝╚═╝  ╚═╝ ╚═════╝   ╚═╝   ╚═╝  ╚═══╝  ╚═╝   ╚═╝      ╚═╝
 *
 * Implemented by Lisa ❤️, Now shut the fuck up and cry about your fucking skills kid 🥀
 * This is just the beginning.
 */

package com.project.lumina.client.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import net.lenni0451.commons.httpclient.HttpClient
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
import net.raphimc.minecraftauth.util.MicrosoftConstants
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

@SuppressLint("SetJavaScriptEnabled")
class RealmsAuthWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {

    private val handler = Handler(Looper.getMainLooper())
    private var authFlow: CompletableFuture<StepFullBedrockSession.FullBedrockSession>? = null
    private var deviceCode: String? = null
    private var verificationUri: String? = null
    
    var callback: ((StepFullBedrockSession.FullBedrockSession?, String?) -> Unit)? = null

    companion object {
        private const val TAG = "RealmsAuthWebView"
        private val BEDROCK_REALMS_AUTH_FLOW = MinecraftAuth.builder()
            .withClientId(MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID)
            .withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
            .deviceCode()
            .withDeviceToken("Android")
            .sisuTitleAuthentication(MicrosoftConstants.BEDROCK_XSTS_RELYING_PARTY)
            .buildMinecraftBedrockChainStep(true, true)
    }

    init {
        CookieManager.getInstance().removeAllCookies(null)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            allowContentAccess = true
            allowFileAccess = false
            databaseEnabled = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }
        webViewClient = RealmsAuthWebViewClient()
    }

    fun startAuthentication(httpClient: HttpClient) {
        showLoadingPage("Initiating authentication...")
        
        thread {
            try {
                Log.d(TAG, "Starting Realms authentication flow")
                
                authFlow = CompletableFuture.supplyAsync {
                    try {
                        BEDROCK_REALMS_AUTH_FLOW.getFromInput(httpClient, StepMsaDeviceCode.MsaDeviceCodeCallback { msaDeviceCode ->
                            Log.d(TAG, "Device code received: ${msaDeviceCode.userCode}")
                            deviceCode = msaDeviceCode.userCode
                            verificationUri = msaDeviceCode.verificationUri
                            
                            handler.post {
                                val urlWithCode = "${msaDeviceCode.verificationUri}?otc=${msaDeviceCode.userCode}"
                                Log.d(TAG, "Loading verification URL: $urlWithCode")
                                loadUrl(urlWithCode)
                            }
                        }) as StepFullBedrockSession.FullBedrockSession
                    } catch (e: Exception) {
                        Log.e(TAG, "Authentication flow error", e)
                        throw e
                    }
                }
                
                authFlow?.thenAccept { session ->
                    Log.d(TAG, "Authentication completed successfully")
                    handler.post {
                        if (session.realmsXsts != null) {
                            callback?.invoke(session, null)
                        } else {
                            callback?.invoke(null, "Authentication succeeded but Realms token missing")
                        }
                    }
                }?.exceptionally { throwable ->
                    Log.e(TAG, "Authentication failed", throwable)
                    handler.post {
                        callback?.invoke(null, throwable.message ?: "Authentication failed")
                    }
                    null
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start authentication", e)
                handler.post {
                    callback?.invoke(null, "Failed to start authentication: ${e.message}")
                }
            }
        }
    }

    private fun showLoadingPage(message: String) {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        margin: 0;
                        padding: 0;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        color: white;
                    }
                    .container {
                        text-align: center;
                        padding: 2rem;
                    }
                    .spinner {
                        border: 4px solid rgba(255,255,255,0.3);
                        border-radius: 50%;
                        border-top: 4px solid white;
                        width: 40px;
                        height: 40px;
                        animation: spin 1s linear infinite;
                        margin: 0 auto 1rem;
                    }
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                    h2 { margin: 0 0 1rem 0; }
                    p { margin: 0; opacity: 0.8; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="spinner"></div>
                    <h2>Minecraft Realms</h2>
                    <p>$message</p>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    inner class RealmsAuthWebViewClient : WebViewClient() {
        
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            Log.d(TAG, "URL loading: $url")

            when {
                url.contains("microsoft.com") || url.contains("live.com") || url.contains("login.microsoftonline.com") -> {
                    return false
                }
                url.contains("success") || url.contains("approved") || url.contains("authenticated") || url.contains("deviceloginsuccess") -> {
                    Log.d(TAG, "Authentication success detected in URL")
                    showLoadingPage("Authentication successful! Completing setup...")
                    return true
                }
                url.contains("error") || url.contains("denied") || url.contains("cancel") || url.contains("deviceloginfail") -> {
                    Log.d(TAG, "Authentication cancelled or failed")
                    callback?.invoke(null, "Authentication was cancelled or failed")
                    return true
                }
                else -> return false
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            Log.d(TAG, "Page finished loading: $url")
            
            if (url.contains("devicelogin") && deviceCode != null) {
                val script = """
                    (function() {
                        function fillCodeAndSubmit() {
                            var codeInput = document.querySelector('input[name="otc"], input[type="text"], #otc, input[placeholder*="code"], input[placeholder*="Code"]');
                            if (codeInput && codeInput.value === '') {
                                codeInput.value = '$deviceCode';
                                codeInput.focus();
                                codeInput.dispatchEvent(new Event('input', { bubbles: true }));
                                codeInput.dispatchEvent(new Event('change', { bubbles: true }));
                                codeInput.dispatchEvent(new Event('keyup', { bubbles: true }));

                                setTimeout(function() {
                                    var submitButton = document.querySelector('input[type="submit"], button[type="submit"], .btn-primary, button:contains("Next"), button:contains("Continue"), button:contains("Submit")');
                                    if (submitButton) {
                                        submitButton.click();
                                    }
                                }, 1500);
                            }
                        }

                        if (document.readyState === 'loading') {
                            document.addEventListener('DOMContentLoaded', fillCodeAndSubmit);
                        } else {
                            fillCodeAndSubmit();
                        }

                        setTimeout(fillCodeAndSubmit, 2000);
                    })();
                """.trimIndent()

                evaluateJavascript(script, null)
            }
        }
    }

    fun cancelAuthentication() {
        authFlow?.cancel(true)
        authFlow = null
        deviceCode = null
        verificationUri = null
    }
}
