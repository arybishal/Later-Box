package com.laterbox.data.demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.laterbox.data.storage.MediaStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Generates local, self-contained, deterministic synthetic image fixtures for testing
 * media cards, thumbnails, Coil rendering, and Rich Notes inline images without network calls.
 */
class DemoImageFixtureGenerator(
    private val context: Context,
    private val mediaStorageManager: MediaStorageManager
) {

    data class FixtureSpec(
        val fileName: String,
        val title: String,
        val subtitle: String,
        val width: Int,
        val height: Int,
        val startColor: Int,
        val endColor: Int,
        val accentColor: Int
    )

    companion object {
        fun rgb(r: Int, g: Int, b: Int): Int =
            (0xFF shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)

        val DEFAULT_FIXTURE_SPECS = listOf(
            FixtureSpec(
                fileName = "demo_img_architecture.jpg",
                title = "Architectural Blueprint",
                subtitle = "Modern Minimalist Spatial Geometry",
                width = 1280,
                height = 720,
                startColor = rgb(24, 28, 36),
                endColor = rgb(44, 52, 68),
                accentColor = rgb(54, 162, 235)
            ),
            FixtureSpec(
                fileName = "demo_img_mountain_sunset.jpg",
                title = "Alpine Horizon",
                subtitle = "Warm Dusk Gradient & Topography",
                width = 1280,
                height = 720,
                startColor = rgb(60, 20, 40),
                endColor = rgb(210, 90, 60),
                accentColor = rgb(255, 180, 80)
            ),
            FixtureSpec(
                fileName = "demo_img_ui_wireframe.jpg",
                title = "Mobile UI Wireframe",
                subtitle = "Design System & Component Specs",
                width = 720,
                height = 1280,
                startColor = rgb(18, 20, 24),
                endColor = rgb(32, 36, 44),
                accentColor = rgb(22, 135, 217)
            ),
            FixtureSpec(
                fileName = "demo_img_audio_studio.jpg",
                title = "Studio Synthesizer",
                subtitle = "Frequency Spectrum & Dial Controls",
                width = 1024,
                height = 768,
                startColor = rgb(30, 24, 38),
                endColor = rgb(72, 48, 88),
                accentColor = rgb(186, 104, 200)
            ),
            FixtureSpec(
                fileName = "demo_img_coffee_recipe.jpg",
                title = "Pour-Over Ratio Chart",
                subtitle = "1:16 Brew Parameter Optimization",
                width = 800,
                height = 800,
                startColor = rgb(42, 28, 20),
                endColor = rgb(96, 64, 46),
                accentColor = rgb(212, 160, 112)
            ),
            FixtureSpec(
                fileName = "demo_img_workspace_setup.jpg",
                title = "Desk Ergonomics",
                subtitle = "Dual Monitor Layout & Focal Distance",
                width = 1280,
                height = 720,
                startColor = rgb(26, 32, 40),
                endColor = rgb(50, 62, 78),
                accentColor = rgb(80, 190, 150)
            ),
            FixtureSpec(
                fileName = "demo_img_data_pipeline.jpg",
                title = "Data Lakehouse Architecture",
                subtitle = "Bronze, Silver, Gold Flow Hierarchy",
                width = 1280,
                height = 720,
                startColor = rgb(16, 26, 36),
                endColor = rgb(36, 60, 84),
                accentColor = rgb(0, 180, 216)
            ),
            FixtureSpec(
                fileName = "demo_img_typography_specimen.jpg",
                title = "Typographic Hierarchy",
                subtitle = "Serif & Sans Proportional Scales",
                width = 1024,
                height = 768,
                startColor = rgb(28, 28, 30),
                endColor = rgb(54, 54, 58),
                accentColor = rgb(240, 240, 245)
            ),
            FixtureSpec(
                fileName = "demo_img_kyoto_travel.jpg",
                title = "Kyoto Bamboo Groves",
                subtitle = "Arashiyama Morning Path & Light",
                width = 720,
                height = 1280,
                startColor = rgb(18, 36, 24),
                endColor = rgb(40, 80, 54),
                accentColor = rgb(120, 200, 130)
            ),
            FixtureSpec(
                fileName = "demo_img_plant_care.jpg",
                title = "Monstera Care Guide",
                subtitle = "Indirect Light & Hydration Schedule",
                width = 800,
                height = 800,
                startColor = rgb(20, 36, 28),
                endColor = rgb(46, 82, 64),
                accentColor = rgb(144, 216, 160)
            ),
            FixtureSpec(
                fileName = "demo_img_camera_rig.jpg",
                title = "35mm Prime Optics",
                subtitle = "Aperture Blades & Chromatic Profile",
                width = 1024,
                height = 768,
                startColor = rgb(22, 22, 26),
                endColor = rgb(48, 48, 56),
                accentColor = rgb(235, 87, 87)
            ),
            FixtureSpec(
                fileName = "demo_img_running_shoes.jpg",
                title = "Trail Traction Geometry",
                subtitle = "5mm Vibram Lugs & Midsole Cushion",
                width = 800,
                height = 800,
                startColor = rgb(36, 28, 22),
                endColor = rgb(84, 62, 44),
                accentColor = rgb(242, 153, 74)
            ),
            FixtureSpec(
                fileName = "demo_img_book_collection.jpg",
                title = "Reading Queue 2026",
                subtitle = "Systems Thinking, Design & History",
                width = 1280,
                height = 720,
                startColor = rgb(28, 22, 34),
                endColor = rgb(64, 48, 80),
                accentColor = rgb(170, 130, 210)
            ),
            FixtureSpec(
                fileName = "demo_img_circuit_board.jpg",
                title = "RISC-V Microarchitecture",
                subtitle = "Trace Bus & Peripheral Interface",
                width = 1280,
                height = 720,
                startColor = rgb(16, 32, 28),
                endColor = rgb(32, 70, 60),
                accentColor = rgb(64, 224, 180)
            ),
            FixtureSpec(
                fileName = "demo_img_sourdough_bake.jpg",
                title = "Wild Fermentation Crumb",
                subtitle = "82% Hydration Tartine Protocol",
                width = 800,
                height = 800,
                startColor = rgb(46, 34, 24),
                endColor = rgb(112, 80, 52),
                accentColor = rgb(230, 180, 120)
            )
        )
    }

    val fixtureSpecs = DEFAULT_FIXTURE_SPECS

    /**
     * Generates all 15 image fixtures in internal media storage if not already present.
     * Returns a map from fileName to absolute path.
     */
    suspend fun generateAllFixtures(): Map<String, String> = withContext(Dispatchers.IO) {
        val mediaDir = mediaStorageManager.getMediaDirectory()
        val result = mutableMapOf<String, String>()

        for (spec in fixtureSpecs) {
            val file = File(mediaDir, spec.fileName)
            if (!file.exists() || file.length() == 0L) {
                createImageFile(spec, file)
            }
            result[spec.fileName] = file.absolutePath
        }

        result
    }

    private fun createImageFile(spec: FixtureSpec, targetFile: File) {
        val bitmap = Bitmap.createBitmap(spec.width, spec.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background Gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, spec.width.toFloat(), spec.height.toFloat(),
                spec.startColor, spec.endColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, spec.width.toFloat(), spec.height.toFloat(), bgPaint)

        // 2. Subtle Grid Overlay Pattern
        val gridPaint = Paint().apply {
            color = Color.WHITE
            alpha = 15
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val step = 80f
        var x = 0f
        while (x < spec.width) {
            canvas.drawLine(x, 0f, x, spec.height.toFloat(), gridPaint)
            x += step
        }
        var y = 0f
        while (y < spec.height) {
            canvas.drawLine(0f, y, spec.width.toFloat(), y, gridPaint)
            y += step
        }

        // 3. Central Graphic Elements
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.accentColor
            alpha = 40
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.accentColor
            alpha = 180
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        val centerX = spec.width / 2f
        val centerY = spec.height / 2f - 40f
        val radius = minOf(spec.width, spec.height) * 0.22f

        canvas.drawCircle(centerX, centerY, radius, shapePaint)
        canvas.drawCircle(centerX, centerY, radius, strokePaint)

        // Inner geometric accent
        val rectSize = radius * 0.85f
        val rectF = RectF(centerX - rectSize, centerY - rectSize, centerX + rectSize, centerY + rectSize)
        canvas.drawRoundRect(rectF, 24f, 24f, strokePaint)

        // 4. Later Box Synthetic Badge Pill
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = 140
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(48f, 48f, 320f, 108f)
        canvas.drawRoundRect(badgeRect, 20f, 20f, badgePaint)

        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
        }
        canvas.drawText("LATER BOX DEMO", 68f, 85f, badgeTextPaint)

        // 5. Title & Subtitle Card at bottom
        val cardHeight = 160f
        val cardMargin = 48f
        val bottomCardRect = RectF(
            cardMargin,
            spec.height - cardHeight - cardMargin,
            spec.width - cardMargin,
            spec.height - cardMargin
        )
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = 160
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(bottomCardRect, 28f, 28f, cardBgPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 42f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = rgb(200, 205, 215)
            textSize = 26f
        }

        canvas.drawText(spec.title, bottomCardRect.left + 36f, bottomCardRect.top + 64f, titlePaint)
        canvas.drawText(spec.subtitle, bottomCardRect.left + 36f, bottomCardRect.top + 114f, subtitlePaint)

        // Write to target file
        FileOutputStream(targetFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()
    }
}
