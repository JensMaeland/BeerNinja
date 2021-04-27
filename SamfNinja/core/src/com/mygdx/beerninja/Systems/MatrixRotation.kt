package com.mygdx.beerninja.Systems

import kotlin.math.cos
import kotlin.math.sin

/*
MatrixRotation SYSTEM, used to modify components
Part of the ECS pattern

 */

class MatrixRotation {
    fun rotateX(x: Float, y: Float, centerX: Float, centerY: Float, angle: Float): Float {
        val originX = x - centerX
        val originY = y - centerY
        val rotatedX = cos(angle / 360 * 6.28).toFloat() * originX + sin(angle / 360 * 6.28).toFloat() * originY
        return rotatedX + originX + x
    }

    fun rotateY(x: Float, y: Float, centerX: Float, centerY: Float, angle: Float): Float {
        val originX = x - centerX
        val originY = y - centerY
        val rotatedY = -sin(angle / 360 * 6.28).toFloat() * originX + cos(angle / 360 * 6.28).toFloat() * originY
        return rotatedY + originY + y
    }
}