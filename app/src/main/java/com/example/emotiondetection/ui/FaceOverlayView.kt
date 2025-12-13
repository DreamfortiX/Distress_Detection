package com.example.emotiondetection.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class FaceOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val facePaint: Paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val labelPaint: Paint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        isAntiAlias = true
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }

    private val faceRects = mutableListOf<RectF>()
    private val faceLabels = mutableListOf<String>()

    fun updateFaces(faces: List<Pair<RectF, String>>) {
        faceRects.clear()
        faceLabels.clear()
        
        faces.forEach { (rect, label) ->
            faceRects.add(rect)
            faceLabels.add(label)
        }
        
        invalidate()
    }

    fun clearFaces() {
        faceRects.clear()
        faceLabels.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        faceRects.forEachIndexed { index, rect ->
            // Draw face rectangle
            canvas.drawRect(rect, facePaint)
            
            // Draw emotion label above the rectangle
            if (index < faceLabels.size) {
                val label = faceLabels[index]
                val textX = rect.left
                val textY = rect.top - 10f
                canvas.drawText(label, textX, textY, labelPaint)
            }
        }
    }
}
