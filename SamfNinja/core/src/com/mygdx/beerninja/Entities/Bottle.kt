package com.mygdx.beerninja.Entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class Bottle(var id: Int, var bottlePlayerId: String, y: Int, velocity: Int, private var bottleSpin: Float, var beerSpawnTime: Float, scale: Int, private var myPlayerId: String, private var textures: HashMap<String, Texture>) {
    var texture: TextureRegion? = null
    var collision = false
    var xStartPos: Int = 0
    private var yStartPos: Int
    private var bottleVelocity: Int
    private var hitbox = Hitbox(textures)
    private var postCollisionVelocity = 50

    private fun getTexture(id: Int, player: String, me: String): TextureRegion {
        val beerTexture: Texture? = when {
            id == 420 -> {
                textures["colada"]
            }
            id >= 50 -> {
                textures["dahls"]
            }
            player == me -> {
                textures["pils"]
            }
            else -> {
                textures["dag"]
            }
        }
        return TextureRegion(beerTexture)
    }

    private fun getXStart(id: Int, player: String, me: String): Int {
        return if (id >= 50) {
            if (id % 2 == 0) {
                -texture!!.regionWidth
            } else Gdx.graphics.width
        } else if (player == me) {
            -texture!!.regionWidth
        } else {
            Gdx.graphics.width
        }
    }

    fun getXOffset(gameTime: Float): Float {
        val offset = gameTime - beerSpawnTime
        var direction = 1

        // powerUp bottles
        if (id >= 50 && id % 2 != 0) {
            direction = -1
        }

        // enemy bottles
        if (bottlePlayerId != myPlayerId) {
            direction = -1
        }

        return if (!collision) {
            xStartPos + offset * direction * bottleVelocity
        } else xStartPos + offset * direction * postCollisionVelocity
    }

    fun getYOffset(gameTime: Float): Float {
        val offset = (gameTime - beerSpawnTime) / 2
        return yStartPos - offset * offset * 3 * bottleVelocity
    }

    fun getHitbox(gameTime: Float, drawer: SpriteBatch, devMode: Boolean, scale: Int): Hitbox {
        val beerWidth = texture!!.regionWidth * scale
        val beerHeight = texture!!.regionHeight * scale
        val minX = getXOffset(gameTime)
        val minY = getYOffset(gameTime)
        val spinAngle = getSpin(gameTime)
        hitbox.updateHitbox(minX, minY, beerWidth, beerHeight, spinAngle)

        // draw the hitboxes in devMode
        if (devMode) {
            drawer.draw(hitbox.texture, hitbox.left, hitbox.top)
            drawer.draw(hitbox.texture, hitbox.right, hitbox.bottom)
            drawer.draw(hitbox.texture, hitbox.left, hitbox.bottom)
            drawer.draw(hitbox.texture, hitbox.right, hitbox.top)
        }
        return hitbox
    }

    fun getSpin(gameTime: Float): Float {
        val offset = gameTime - beerSpawnTime
        return if (bottlePlayerId == myPlayerId) {
            if (!collision) {
                -offset * bottleSpin * 200
            } else bottleSpin * 30
        } else {
            if (!collision) {
                offset * bottleSpin * 200
            } else -bottleSpin * 30
        }
    }

    init {
        texture = getTexture(id, bottlePlayerId, myPlayerId)
        xStartPos = getXStart(id, bottlePlayerId, myPlayerId)
        yStartPos = Gdx.graphics.height - scale * y
        bottleVelocity = velocity * scale
    }
}