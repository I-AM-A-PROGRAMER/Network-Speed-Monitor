package com.riyo.networkspeedmonitor.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import com.riyo.networkspeedmonitor.model.SpeedData
import java.util.Locale

/**
 * Dynamically draws the current network speed digits onto a small monochrome bitmap
 * with maximized font size and condensed bold typography for optimal visibility
 * directly in the Android status bar.
 */
object SpeedIconGenerator {

    private const val ICON_SIZE = 96

    fun generateSpeedIcon(speed: SpeedData): IconCompat {
        val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val isUploadPrimary = speed.downloadMBps < 0.01 && speed.uploadMBps >= 0.01
        val valueToShow = if (isUploadPrimary) speed.uploadMBps else speed.downloadMBps
        val arrow = if (isUploadPrimary) "↑" else ""

        val speedText = when {
            valueToShow >= 100.0 -> String.format(Locale.US, "%.0f", valueToShow)
            valueToShow >= 10.0 -> String.format(Locale.US, "%.1f", valueToShow)
            valueToShow >= 0.01 -> String.format(Locale.US, "%.1f", valueToShow)
            else -> "0.0"
        }

        val displayValue = arrow + speedText
        val unitText = "M/s"

        // Use condensed bold typeface to maximize glyph height and legibility
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textSize = when (displayValue.length) {
                1, 2 -> 62f
                3 -> 54f
                4 -> 46f
                else -> 38f
            }
        }

        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textSize = 30f
        }

        // Draw speed value on upper area (vertically centered)
        canvas.drawText(displayValue, ICON_SIZE / 2f, 54f, valuePaint)
        // Draw unit on lower area
        canvas.drawText(unitText, ICON_SIZE / 2f, 88f, unitPaint)

        return IconCompat.createWithBitmap(bitmap)
    }
}
