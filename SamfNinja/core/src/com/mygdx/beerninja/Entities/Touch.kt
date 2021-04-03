package com.mygdx.beerninja

import com.badlogic.gdx.graphics.Texture

class Touch(var id: Int, enemy: Boolean, private var textures: HashMap<String, Texture>) {
    var x: Int = 0
    var y: Int = 0
    var display = false
    var texture: Texture? = null

    private fun getTexture(enemy: Boolean): Texture? {
        return if (!enemy) {
            textures["myTouch"]
        } else textures["enemyTouch"]
    }

    init {
        texture = getTexture(enemy)
    }
}
