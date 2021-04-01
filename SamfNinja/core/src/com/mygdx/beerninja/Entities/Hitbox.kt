package com.mygdx.beerninja

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Texture
import kotlin.math.cos
import kotlin.math.sin

class Hitbox : ApplicationAdapter() {
    var left: Float = 0.0f
    var top: Float = 0.0f
    var right: Float = 0.0f
    var bottom: Float = 0.0f
    var texture: Texture? = null

    private fun rotateX(x: Float, y: Float, centerX: Float, centerY: Float, angle: Float): Float {
        val originX = x - centerX
        val originY = y - centerY
        val rotatedX = cos(angle / 360 * 6.28).toFloat() * originX + sin(angle / 360 * 6.28).toFloat() * originY
        return rotatedX + originX + x
    }

    private fun rotateY(x: Float, y: Float, centerX: Float, centerY: Float, angle: Float): Float {
        val originX = x - centerX
        val originY = y - centerY
        val rotatedY = -sin(angle / 360 * 6.28).toFloat() * originX + cos(angle / 360 * 6.28).toFloat() * originY
        return rotatedY + originY + y
    }

    fun updateHitbox(minX: Float, minY: Float, width: Int, height: Int, spin: Float) {
        val maxX = minX + width
        val maxY = minY + height
        val rotatedMinX = rotateX(minX, minY, minX - width / 2, minY - height / 2, spin)
        val rotatedMinY = rotateY(minX, minY, minX - width / 2, minY - height / 2, spin)
        val rotatedMaxX = rotateX(maxX, maxY, maxX + width / 2, maxY + height / 2, spin)
        val rotatedMaxY = rotateY(maxX, maxY, maxX + width / 2, maxY + height / 2, spin)

        if (rotatedMaxX > rotatedMinX) {
            right = rotatedMaxX
            left = rotatedMinX
        } else {
            right = rotatedMinX
            left = rotatedMaxX
        }
        if (rotatedMaxY > rotatedMinY) {
            top = rotatedMaxY
            bottom = rotatedMinY
        } else {
            top = rotatedMinY
            bottom = rotatedMaxY
        }
    }

    override fun create() {
        texture = Texture("touch.png")
    }

}