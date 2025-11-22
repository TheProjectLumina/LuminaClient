package com.project.lumina.client.game.module.impl.visual

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.Player
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.render.ESPRenderOverlayView
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.R
import org.cloudburstmc.math.matrix.Matrix4f
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.cos
import kotlin.math.sin

class ESPElement : Element(
    name = "esp",
    category = CheatCategory.Visual,
    displayNameResId = R.string.esp_module_name
) {
    companion object {
        private var renderView: ESPRenderOverlayView? = null

        fun setRenderView(view: ESPRenderOverlayView) {
            renderView = view
        }
    }

    private var cachedBoxPaint: Paint? = null
    private var cachedTextPaint: Paint? = null
    private var cachedOutlinePaint: Paint? = null
    private var lastUpdateTime = 0L

    private val fov by floatValue("FOV", 110f, 40f..110f)
    private val strokeWidth by floatValue("Stroke Width", 2f, 1f..10f)
    private val cornerRadius by floatValue("Corner Radius", 4f, 0f..20f)

    private val rgbGradient by boolValue("RGB Gradient", false)

    private val showAllEntities by boolValue("Show All Entities", false)
    private val showDistance by boolValue("Show Distance", true)
    private val showNames by boolValue("Show Names", true)

    private val use3DBox by boolValue("3D Box", true)

    private val tracers by boolValue("Tracers", false)
    private val tracerBottom by boolValue("Tracer Bottom", true)
    private val tracerTop by boolValue("Tracer Top", false)
    private val tracerCenter by boolValue("Tracer Center", false)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
    }

    override fun onEnabled() {
        super.onEnabled()
        if (isSessionCreated) {
            if (renderView == null) {
                renderView = ESPRenderOverlayView.createAndShow()
                ESPElement.setRenderView(renderView!!)
            }
            renderView?.post {
                renderView?.invalidate()
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        renderView?.let {
            ESPRenderOverlayView.dismissOverlay(it)
            renderView = null
        }
    }

    private fun rotateX(angle: Float): Matrix4f {
        val rad = Math.toRadians(angle.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()

        return Matrix4f.from(
            1f, 0f, 0f, 0f,
            0f, c, -s, 0f,
            0f, s, c, 0f,
            0f, 0f, 0f, 1f
        )
    }

    private fun rotateY(angle: Float): Matrix4f {
        val rad = Math.toRadians(angle.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()

        return Matrix4f.from(
            c, 0f, s, 0f,
            0f, 1f, 0f, 0f,
            -s, 0f, c, 0f,
            0f, 0f, 0f, 1f
        )
    }

    private fun getEntityBoxVertices(entity: Entity): Array<Vector3f> {
        val width = 0.6f
        val height = 1.8f 

        val pos = entity.vec3Position
        val halfWidth = width / 2f

        val yPos = if (entity is Player) {
            pos.y - 1.62f
        } else {
            pos.y
        }

        return arrayOf(
            Vector3f.from(pos.x - halfWidth, yPos, pos.z - halfWidth),          
            Vector3f.from(pos.x - halfWidth, yPos + height, pos.z - halfWidth), 
            Vector3f.from(pos.x + halfWidth, yPos + height, pos.z - halfWidth), 
            Vector3f.from(pos.x + halfWidth, yPos, pos.z - halfWidth),          
            Vector3f.from(pos.x - halfWidth, yPos, pos.z + halfWidth),          
            Vector3f.from(pos.x - halfWidth, yPos + height, pos.z + halfWidth), 
            Vector3f.from(pos.x + halfWidth, yPos + height, pos.z + halfWidth), 
            Vector3f.from(pos.x + halfWidth, yPos, pos.z + halfWidth)           
        )
    }

    private fun worldToScreen(pos: Vector3f, viewProjMatrix: Matrix4f, screenWidth: Int, screenHeight: Int): Vector2f? {
        val w = viewProjMatrix.get(3, 0) * pos.x +
                viewProjMatrix.get(3, 1) * pos.y +
                viewProjMatrix.get(3, 2) * pos.z +
                viewProjMatrix.get(3, 3)

        if (w < 0.01f) return null

        val inverseW = 1f / w

        val screenX = screenWidth / 2f + (0.5f * ((viewProjMatrix.get(0, 0) * pos.x +
                viewProjMatrix.get(0, 1) * pos.y +
                viewProjMatrix.get(0, 2) * pos.z +
                viewProjMatrix.get(0, 3)) * inverseW) * screenWidth + 0.5f)

        val screenY = screenHeight / 2f - (0.5f * ((viewProjMatrix.get(1, 0) * pos.x +
                viewProjMatrix.get(1, 1) * pos.y +
                viewProjMatrix.get(1, 2) * pos.z +
                viewProjMatrix.get(1, 3)) * inverseW) * screenHeight + 0.5f)

        return Vector2f.from(screenX, screenY)
    }

    private fun shouldRenderEntity(entity: Entity): Boolean {
        if (entity == session.localPlayer) return false
        if (!showAllEntities && entity !is Player) return false
        return true
    }

    private fun createEnhancedPaint(time: Float, offset: Float): Paint {
        return Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = this@ESPElement.strokeWidth
            isAntiAlias = true
            isDither = true
            pathEffect = CornerPathEffect(cornerRadius)

            color = if (rgbGradient) {
                getRGBGradientColor(time, offset)
            } else {
                Color.RED
            }
        }
    }

    private fun createTextPaint(time: Float, offset: Float): Paint {
        return Paint().apply {
            style = Paint.Style.FILL
            textSize = 32f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER

            color = if (rgbGradient) {
                getRGBGradientColor(time, offset + 0.5f)
            } else {
                Color.RED
            }
        }
    }

    private fun getRGBGradientColor(time: Float, offset: Float): Int {
        val hue = ((time * 60f + offset * 120f) % 360f)
        val saturation = 0.8f
        val brightness = 1.0f

        val hsv = floatArrayOf(hue, saturation, brightness)
        return android.graphics.Color.HSVToColor(hsv)
    }

    private fun createTextOutlinePaint(): Paint {
        return Paint().apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            textSize = 32f
            strokeWidth = 3f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
    }

    fun render(canvas: Canvas) {
        if (!isEnabled || !isSessionCreated) return

        val player = session.localPlayer
        val entities = if (showAllEntities) {
            session.level.entityMap.values
        } else {
            session.level.entityMap.values.filterIsInstance<Player>()
        }

        if (entities.isEmpty()) return

        val screenWidth = canvas.width
        val screenHeight = canvas.height

        val viewProjMatrix = Matrix4f.createPerspective(fov,
            screenWidth.toFloat() / screenHeight, 0.1f, 128f)
            .mul(Matrix4f.createTranslation(player.vec3Position)
                .mul(rotateY(-player.rotationYaw - 180))
                .mul(rotateX(-player.rotationPitch))
                .invert())

        val currentTime = System.currentTimeMillis()
        val animationTime = (currentTime / 6000f) % 1f

        entities.forEachIndexed { index, entity ->
            if (shouldRenderEntity(entity)) {
                val entityOffset = index * 0.3f
                val paint = createEnhancedPaint(animationTime, entityOffset)
                val textPaint = createTextPaint(animationTime, entityOffset)
                val outlinePaint = createTextOutlinePaint()

                drawEntityBox(entity, viewProjMatrix, screenWidth, screenHeight, canvas, paint, textPaint, outlinePaint)
            }
        }
    }

    private fun drawEntityBox(entity: Entity, viewProjMatrix: Matrix4f,
                              screenWidth: Int, screenHeight: Int,
                              canvas: Canvas, paint: Paint, textPaint: Paint, outlinePaint: Paint) {
        val boxVertices = getEntityBoxVertices(entity)
        var minX = screenWidth.toDouble()
        var minY = screenHeight.toDouble()
        var maxX = 0.0
        var maxY = 0.0
        val screenPositions = mutableListOf<Vector2f>()

        boxVertices.forEach { vertex ->
            val screenPos = worldToScreen(vertex, viewProjMatrix, screenWidth, screenHeight)
                ?: return@forEach
            screenPositions.add(screenPos)
            minX = minX.coerceAtMost(screenPos.x.toDouble())
            minY = minY.coerceAtMost(screenPos.y.toDouble())
            maxX = maxX.coerceAtLeast(screenPos.x.toDouble())
            maxY = maxY.coerceAtLeast(screenPos.y.toDouble())
        }

        if (!(minX >= screenWidth || minY >= screenHeight || maxX <= 0 || maxY <= 0)) {
            if (use3DBox) {
                draw3DBox(canvas, paint, screenPositions)
            }

            if (tracers) {
                drawTracer(canvas, paint, screenWidth, screenHeight, minX, minY, maxX, maxY)
            }

            if (showNames || showDistance) {
                drawEntityInfo(canvas, textPaint, outlinePaint, entity, minX, minY, maxX)
            }
        }
    }



    private fun draw3DBox(canvas: Canvas, paint: Paint, screenPositions: List<Vector2f>) {
        if (screenPositions.size < 8) return

        val edges = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0,  
            4 to 5, 5 to 6, 6 to 7, 7 to 4,  
            0 to 4, 1 to 5, 2 to 6, 3 to 7   
        )

        edges.forEach { (start, end) ->
            val startPos = screenPositions[start]
            val endPos = screenPositions[end]

            if (isOnScreen(startPos, canvas) && isOnScreen(endPos, canvas)) {
                //slight padding
                val padding = paint.strokeWidth / 2
                canvas.drawLine(
                    startPos.x.coerceIn(padding, canvas.width - padding),
                    startPos.y.coerceIn(padding, canvas.height - padding),
                    endPos.x.coerceIn(padding, canvas.width - padding),
                    endPos.y.coerceIn(padding, canvas.height - padding),
                    paint
                )
            }
        }
    }

    private fun isOnScreen(pos: Vector2f, canvas: Canvas): Boolean {
        return pos.x >= 0 && pos.x <= canvas.width &&
                pos.y >= 0 && pos.y <= canvas.height
    }



    private fun drawTracer(canvas: Canvas, paint: Paint, screenWidth: Int, screenHeight: Int, minX: Double, minY: Double, maxX: Double, maxY: Double) {
        val start = when {
            tracerBottom -> Vector2f.from(screenWidth / 2f, screenHeight.toFloat())
            tracerTop -> Vector2f.from(screenWidth / 2f, 0f)
            tracerCenter -> Vector2f.from(screenWidth / 2f, screenHeight / 2f)
            else -> Vector2f.from(screenWidth / 2f, screenHeight.toFloat())
        }
        val end = Vector2f.from(
            (minX + maxX).toFloat() / 2,
            (minY + maxY).toFloat() / 2
        )
        canvas.drawLine(start.x, start.y, end.x, end.y, paint)
    }

    @SuppressLint("DefaultLocale")
    private fun drawEntityInfo(canvas: Canvas, textPaint: Paint, outlinePaint: Paint, entity: Entity, minX: Double, minY: Double, maxX: Double) {
        val bgPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val info = buildString {
            if (showNames && entity is Player) {
                append(entity.username)
            }
            if (showDistance) {
                if (isNotEmpty()) append(" | ")
                val distance = entity.vec3Position.distance(session.localPlayer.vec3Position)
                append("${String.format("%.1f", distance)}m")
            }
        }

        val textX = (minX + maxX).toFloat() / 2
        val textY = minY.toFloat() - 10

        val bounds = android.graphics.Rect()
        textPaint.getTextBounds(info, 0, info.length, bounds)

        val padding = 12f
        val bgRect = RectF(
            textX - bounds.width() / 2 - padding,
            textY - bounds.height() - padding,
            textX + bounds.width() / 2 + padding,
            textY + padding
        )
        canvas.drawRoundRect(bgRect, cornerRadius * 2, cornerRadius * 2, bgPaint)

        val shadowPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.argb(100, 0, 0, 0)
            textSize = 32f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(info, textX + 2, textY + 2, shadowPaint)
        canvas.drawText(info, textX, textY, outlinePaint)
        canvas.drawText(info, textX, textY, textPaint)
    }
}