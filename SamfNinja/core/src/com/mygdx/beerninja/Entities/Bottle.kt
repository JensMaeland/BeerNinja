package com.mygdx.beerninja

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class Bottle(var bottleId: Int, var bottlePlayerId: String, y: Int, velocity: Int, private var bottleSpin: Float, var beerSpawnTime: Float, scale: Int, private var myPlayerId: String) : ApplicationAdapter() {
    var texture: TextureRegion? = null
    var collision = false
    var xStartPos: Int = 0
    private var yStartPos: Int
    private var bottleVelocity: Int
    private var hitbox = Hitbox()

    private fun getTexture(id: Int, player: String, me: String): TextureRegion {
        val beerTexture: Texture = when {
            player == "420" -> {
                Texture("colada.png")
            }
            id >= 50 -> {
                Texture("dahls.png")
            }
            player == me -> {
                Texture("pils.png")
            }
            else -> {
                Texture("dag.png")
            }
        }
        return TextureRegion(beerTexture)
    }

    private fun getXPos(id: Int, player: String, me: String): Int {
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
        return if (bottleId >= 50) {
            if (bottleId % 2 == 0) {
                xStartPos + offset * bottleVelocity
            } else xStartPos - offset * bottleVelocity
        } else if (bottlePlayerId == myPlayerId) {
            if (!collision) {
                xStartPos + offset * bottleVelocity
            } else xStartPos + offset * 50
        } else {
            if (!collision) {
                xStartPos - offset * bottleVelocity
            } else xStartPos - offset * 50
        }
    }

    fun getYOffset(gameTime: Float): Float {
        val offset = (gameTime - beerSpawnTime) / 2
        return yStartPos - offset * offset * 3 * bottleVelocity
    }

    fun getHitbox(gameTime: Float, screenDrawer: SpriteBatch, devMode: Boolean, scale: Int): Hitbox {
        val beerWidth = texture!!.regionWidth * scale
        val beerHeight = texture!!.regionHeight * scale
        val minX = getXOffset(gameTime)
        val minY = getYOffset(gameTime)
        val spinAngle = getSpin(gameTime)
        hitbox.updateHitbox(minX, minY, beerWidth, beerHeight, spinAngle)

        // draw the hitboxes in devMode
        if (devMode) {
            screenDrawer.begin()
            screenDrawer.draw(hitbox.texture, hitbox.left, hitbox.top)
            screenDrawer.draw(hitbox.texture, hitbox.right, hitbox.bottom)
            screenDrawer.draw(hitbox.texture, hitbox.left, hitbox.bottom)
            screenDrawer.draw(hitbox.texture, hitbox.right, hitbox.top)
            screenDrawer.end()
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

    override fun create() {
        texture = getTexture(bottleId, bottlePlayerId, myPlayerId)
        xStartPos = getXPos(bottleId, bottlePlayerId, myPlayerId)
    }

    init {
        yStartPos = Gdx.graphics.height - scale * y
        bottleVelocity = velocity * scale
    }
}