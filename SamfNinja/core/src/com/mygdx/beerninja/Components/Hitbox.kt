package com.mygdx.beerninja.Components

import com.badlogic.gdx.graphics.Texture
import com.mygdx.beerninja.Systems.MatrixRotation

/*
Hitbox / Hitable COMPONENT, represents property to be utilized by entities
Part of the ECS pattern

Modified in runtime by the MatrixRotation system
 */

class Hitbox(textures: HashMap<String, Texture>) {
    var left: Float = 0.0f
    var top: Float = 0.0f
    var right: Float = 0.0f
    var bottom: Float = 0.0f
    var texture: Texture? = null

    fun updateHitbox(minX: Float, minY: Float, width: Int, height: Int, spin: Float, rotationSystem: MatrixRotation) {
        val maxX = minX + width
        val maxY = minY + height
        val rotatedMinX = rotationSystem.rotateX(minX, minY, minX - width / 2, minY - height / 2, spin)
        val rotatedMinY = rotationSystem.rotateY(minX, minY, minX - width / 2, minY - height / 2, spin)
        val rotatedMaxX = rotationSystem.rotateX(maxX, maxY, maxX + width / 2, maxY + height / 2, spin)
        val rotatedMaxY = rotationSystem.rotateY(maxX, maxY, maxX + width / 2, maxY + height / 2, spin)

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

    init {
        texture = textures["hitbox"]
    }

}