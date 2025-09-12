package com.br.klibras

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<HandLandmarkerResult> = emptyList()
    private val linePaint = Paint()
    private val pointPaint = Paint()
    private val textPaint = Paint()
    private val backgroundPaint = Paint()
    private val textBounds = Rect()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var isFrontCamera: Boolean = false

    init {
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = Color.RED
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        textPaint.color = Color.WHITE
        textPaint.textSize = 48f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.isAntiAlias = true

        backgroundPaint.color = Color.argb(180, 0, 0, 0)
        backgroundPaint.style = Paint.Style.FILL
    }

    fun clear() {
        results = emptyList()
        invalidate()
    }

    fun setResults(
        handLandmarkerResults: List<HandLandmarkerResult>,
        imageHeight: Int,
        imageWidth: Int,
        isFrontCamera: Boolean = false,
        runningMode: RunningMode = RunningMode.LIVE_STREAM,
    ) {
        results = handLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        this.isFrontCamera = isFrontCamera

        when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                scaleFactor = min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                val scaleX = width * 1f / imageWidth
                val scaleY = height * 1f / imageHeight
                scaleFactor = min(scaleX, scaleY)
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var totalHands = 0
        for (result in results) {
            totalHands += result.landmarks().size
        }

        if (totalHands > 0) {
            val text = "Hands identified: $totalHands"

            // Reuse the reallocated Rect object
            textPaint.getTextBounds(text, 0, text.length, textBounds)

            val textX = 20f
            val textY = textBounds.height() + 40f

            canvas.drawRect(
                textX - 10f,
                textY - textBounds.height() - 10f,
                textX + textBounds.width() + 20f,
                textY + 10f,
                backgroundPaint
            )

            canvas.drawText(text, textX, textY, textPaint)
        }

        if (results.isEmpty()) return

        for (result in results) {
            val hands = result.landmarks()
            for (landmarkList in hands) {
                // Draw landmarks
                for (landmark in landmarkList) {
                    val (x, y) = transformCoordinates(landmark.x(), landmark.y())
                    canvas.drawCircle(x, y, LANDMARK_POINT_RADIUS, pointPaint)
                }

                // Draw connections
                HandLandmarker.HAND_CONNECTIONS.forEach { connection ->
                    val start = landmarkList[connection.start()]
                    val end = landmarkList[connection.end()]

                    val (startX, startY) = transformCoordinates(start.x(), start.y())
                    val (endX, endY) = transformCoordinates(end.x(), end.y())

                    canvas.drawLine(startX, startY, endX, endY, linePaint)
                }
            }
        }
    }

    private fun transformCoordinates(normalizedX: Float, normalizedY: Float): Pair<Float, Float> {
        var x = normalizedX
        val y = normalizedY

        if (isFrontCamera) {
            x = 1.0f - x
        }

        val offsetX = (width - imageWidth * scaleFactor) / 2
        val offsetY = (height - imageHeight * scaleFactor) / 2

        val pixelX = x * imageWidth * scaleFactor + offsetX
        val pixelY = y * imageHeight * scaleFactor + offsetY

        return Pair(pixelX, pixelY)
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8f
        private const val LANDMARK_POINT_RADIUS = 10f
    }
}