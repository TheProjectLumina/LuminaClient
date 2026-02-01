package com.project.lumina.client.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.game.module.impl.visual.ESPElement

class ESPRenderOverlayView(context: Context) : View(context) {

    private var isRenderingActive = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isRenderingActive = true
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isRenderingActive = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val activeESPModules = GameManager.elements
            .filterIsInstance<ESPElement>()
            .filter { it.isEnabled && it.isSessionCreated }

        activeESPModules.forEach { it.render(canvas) }

        if (activeESPModules.isNotEmpty()) {
            postInvalidateOnAnimation()
        }
    }

    companion object {
        private var currentOverlay: ESPRenderOverlayView? = null

        fun createAndShow(): ESPRenderOverlayView {
            val context = AppContext.instance
            val overlay = ESPRenderOverlayView(context)
            
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            
            try {
                windowManager.addView(overlay, params)
                currentOverlay = overlay
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return overlay
        }

        fun dismissOverlay(overlay: ESPRenderOverlayView) {
            try {
                val context = AppContext.instance
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.removeView(overlay)
                if (currentOverlay == overlay) {
                    currentOverlay = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}