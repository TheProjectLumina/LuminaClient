/*
 * © Project Lumina 2026 — Licensed under GNU GPLv3
 */

package com.project.lumina.client.overlay.mods

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

data class Position(val x: Float, val y: Float)

data class MinimapEntity(
    val id: Long,
    val position: Position,
    val name: String,
    val imagePath: String?,
    val isPlayer: Boolean = false,
    val lastUpdate: Long = System.currentTimeMillis()
)

class MiniMapOverlay : OverlayWindow() {
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.END
            x = 50
            y = 50
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    // State
    private var centerPosition by mutableStateOf(Position(0f, 0f))
    private var playerRotation by mutableStateOf(0f)
    private var targetRotation by mutableStateOf(0f)
    private var entities by mutableStateOf<Map<Long, MinimapEntity>>(emptyMap())

    // Settings
    var minimapSize by mutableStateOf(100f)
    var minimapZoom by mutableStateOf(1.0f)
    var minimapDotSize by mutableStateOf(5)

    private val rotationSmoothStep = 0.15f
    private val entityTimeout = 5000L
    private val maxEntities = 100

    companion object {
        val overlayInstance by lazy { MiniMapOverlay() }
        private var shouldShowOverlay = false

        fun showOverlay() {
            if (shouldShowOverlay) {
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun dismissOverlay() {
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (enabled) {
                showOverlay()
            } else {
                dismissOverlay()
                overlayInstance.clearAll()
            }
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setCenter(x: Float, y: Float) {
            overlayInstance.centerPosition = Position(x, y)
        }

        fun setPlayerRotation(rotation: Float) {
            overlayInstance.targetRotation = rotation
        }

        fun updateEntity(id: Long, x: Float, y: Float, name: String, imagePath: String?, isPlayer: Boolean = false) {
            overlayInstance.updateEntityInternal(id, x, y, name, imagePath, isPlayer)
        }

        fun removeEntity(id: Long) {
            overlayInstance.removeEntityInternal(id)
        }

        fun setMinimapSize(size: Float) {
            overlayInstance.minimapSize = size
        }

        fun clearAllEntities() {
            overlayInstance.clearAll()
        }
    }

    private fun updateEntityInternal(id: Long, x: Float, y: Float, name: String, imagePath: String?, isPlayer: Boolean) {
        val currentEntities = entities.toMutableMap()


        if (currentEntities.size >= maxEntities && !currentEntities.containsKey(id)) {
            val oldestId = currentEntities.minByOrNull { it.value.lastUpdate }?.key
            if (oldestId != null) {
                currentEntities.remove(oldestId)
            }
        }

        currentEntities[id] = MinimapEntity(
            id = id,
            position = Position(x, y),
            name = name,
            imagePath = imagePath,
            isPlayer = isPlayer,
            lastUpdate = System.currentTimeMillis()
        )

        entities = currentEntities
    }

    private fun removeEntityInternal(id: Long) {
        val currentEntities = entities.toMutableMap()
        currentEntities.remove(id)
        entities = currentEntities
    }

    private fun cleanupStaleEntities() {
        val now = System.currentTimeMillis()
        val currentEntities = entities.toMutableMap()
        val staleIds = currentEntities.filter { (_, entity) ->
            now - entity.lastUpdate > entityTimeout
        }.keys

        staleIds.forEach { currentEntities.remove(it) }

        if (staleIds.isNotEmpty()) {
            entities = currentEntities
        }
    }

    private fun clearAll() {
        entities = emptyMap()
        centerPosition = Position(0f, 0f)
        playerRotation = 0f
        targetRotation = 0f
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return


        LaunchedEffect(targetRotation) {
            while (abs(playerRotation - targetRotation) > 0.001f) {
                var delta = (targetRotation - playerRotation) % (2 * Math.PI).toFloat()
                if (delta > Math.PI) delta -= (2 * Math.PI).toFloat()
                if (delta < -Math.PI) delta += (2 * Math.PI).toFloat()

                playerRotation += delta * rotationSmoothStep
                delay(16L)
            }
        }


        LaunchedEffect(Unit) {
            while (true) {
                delay(2000L)
                cleanupStaleEntities()
            }
        }

        Minimap(centerPosition, playerRotation, entities.values.toList(), minimapSize)
    }

    @Composable
    private fun Minimap(
        center: Position,
        rotation: Float,
        entityList: List<MinimapEntity>,
        size: Float
    ) {
        val dpSize = size.dp
        val rawRadius = size / 2
        val radius = rawRadius * minimapZoom
        val scale = 2f * minimapZoom


        val context = LocalContext.current


        val imageCache = remember { mutableMapOf<String, android.graphics.Bitmap?>() }

        Box(
            modifier = Modifier
                .size(dpSize)
                .background(Mbg, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(dpSize)) {
                val centerX = this.size.width / 2
                val centerY = this.size.height / 2


                val gridColor = MgridColor
                val gridSpacing = this.size.width / 10
                for (i in 1 until 10) {
                    val x = i * gridSpacing
                    drawLine(gridColor, Offset(x, 0f), Offset(x, this.size.height), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, x), Offset(this.size.width, x), strokeWidth = 1f)
                }


                drawLine(MCrosshair, Offset(centerX, 0f), Offset(centerX, this.size.height), strokeWidth = 1.5f)
                drawLine(MCrosshair, Offset(0f, centerY), Offset(this.size.width, centerY), strokeWidth = 1.5f)


                val playerDotRadius = minimapDotSize * minimapZoom
                drawCircle(MPlayerMarker, radius = playerDotRadius, center = Offset(centerX, centerY))


                val northAngle = -rotation
                val northDistance = rawRadius * 0.95f
                val northX = centerX + northDistance * sin(northAngle)
                val northY = centerY - northDistance * cos(northAngle)

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLUE
                    textSize = size * 0.14f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                drawContext.canvas.nativeCanvas.drawText("^", northX, northY - paint.textSize * 0.6f, paint)
                drawContext.canvas.nativeCanvas.drawText("N", northX, northY + paint.textSize * 0.4f, paint)


                entityList.forEach { entity ->
                    val relX = entity.position.x - center.x
                    val relY = entity.position.y - center.y
                    val distance = sqrt(relX * relX + relY * relY) * scale

                    val angle = atan2(relY, relX) - rotation
                    val clampedDistance = if (distance < radius * 0.9f) distance else radius * 0.85f
                    val entityX = centerX + clampedDistance * sin(angle)
                    val entityY = centerY - clampedDistance * cos(angle)

                    if (entity.isPlayer) {

                        val dotRadius = minimapDotSize * minimapZoom * 1.2f
                        drawCircle(
                            color = if (distance < radius * 0.9f) MEntityClose else MEntityFar,
                            radius = dotRadius,
                            center = Offset(entityX, entityY)
                        )

                        drawCircle(
                            color = androidx.compose.ui.graphics.Color.White,
                            radius = dotRadius * 0.4f,
                            center = Offset(entityX, entityY)
                        )
                    } else {

                        val dotRadius = minimapDotSize * minimapZoom * 1.5f


                        drawCircle(
                            color = if (distance < radius * 0.9f) MEntityClose else MEntityFar,
                            radius = dotRadius,
                            center = Offset(entityX, entityY)
                        )


                        if (entity.imagePath != null) {
                            try {
                                val bitmap = imageCache.getOrPut(entity.imagePath) {
                                    try {
                                        val cleanPath = entity.imagePath.removePrefix("/")
                                        val inputStream = context.assets.open(cleanPath)
                                        val bmp = android.graphics.BitmapFactory.decodeStream(inputStream)
                                        inputStream.close()
                                        bmp
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                if (bitmap != null) {
                                    val imageSize = dotRadius * 1.6f
                                    val left = entityX - imageSize / 2
                                    val top = entityY - imageSize / 2
                                    val right = entityX + imageSize / 2
                                    val bottom = entityY + imageSize / 2

                                    val destRect = android.graphics.RectF(left, top, right, bottom)
                                    val bitmapPaint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        isFilterBitmap = true
                                    }
                                    drawContext.canvas.nativeCanvas.drawBitmap(bitmap, null, destRect, bitmapPaint)
                                }
                            } catch (e: Exception) {
                                // Image failed, the circle is already drawn
                            }
                        }
                    }
                }
            }
        }


        DisposableEffect(Unit) {
            onDispose {
                imageCache.values.forEach { it?.recycle() }
                imageCache.clear()
            }
        }
    }
}