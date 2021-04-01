package com.mygdx.beerninja

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Texture

class Touch(var id: Int, private var enemy: Boolean) : ApplicationAdapter() {
    var x: Int = 0
    var y: Int = 0
    var texture: Texture? = null
    var display = false

    private fun getTexture(enemy: Boolean): Texture {
        return if (!enemy) {
            Texture("touch3.png")
        } else Texture("touch.png")
    }

    override fun create() {
        texture = getTexture(enemy)
    }
}