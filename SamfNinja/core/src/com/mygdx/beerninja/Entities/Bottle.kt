package com.mygdx.beerninja.Entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.mygdx.beerninja.Components.Hitbox
import com.mygdx.beerninja.Systems.MatrixRotation
import kotlin.math.abs

/*
Bottle ENTITY, represents sprite to be rendered on screen
Part of the ECS pattern

Uses the Hitbox component
 */

class Bottle(var id: Int, var bottlePlayerId: String, y: Int, velocity: Int, private var bottleSpin: Float, var beerSpawnTime: Float, scale: Int, private var myPlayerId: String, private var textures: HashMap<String, Texture>) {
    var texture: TextureRegion? = null
    var tail: TextureRegion? = null
    var collision = false
    var xStartPos: Int = 0
    private var yStartPos: Int
    private var bottleVelocity: Int
    private var hitbox = Hitbox(textures)
    private var postCollisionVelocity = 50

    private fun getTexture(id: Int, player: String, me: String): TextureRegion {
        val beerTexture: Texture? = when {
            id % 420 == 0 && id != 0 -> {
                textures["samfkort"]
            }
            abs(id) >= 50 -> {
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

    private fun getTail(id: Int, player: String, me: String): TextureRegion {
        val beerTexture: Texture? = when {
            id == 420 -> {
                textures["tail1"]
            }
            id >= 50 -> {
                if (id % 2 == 0) {
                    textures["tail1"]
                } else textures["tail2"]
            }
            player == me -> {
                textures["tail1"]
            }
            else -> {
                textures["tail2"]
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

    fun getTailOffset(bottleOffset: Float): Float {
        // powerUp tails
        if (id >= 50 && id % 2 != 0) {
            return bottleOffset + tail!!.regionWidth - texture!!.regionWidth / 2
        }

        // enemy tails
        if (bottlePlayerId != myPlayerId) {
            return bottleOffset + tail!!.regionWidth - texture!!.regionWidth / 2
        }

        return bottleOffset - tail!!.regionWidth + texture!!.regionWidth / 2
    }

    fun getXOffset(gameTime: Float, scale: Int): Float {
        val offset = gameTime - beerSpawnTime
        var direction = 1

        // tutorial bottles
        if (id < 0) {
            return (Gdx.graphics.width - texture!!.regionWidth * scale) / 2f
        }

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
        // tutorial bottles
        if (id < 0) {
            return (Gdx.graphics.height / 2f) + (Gdx.graphics.width * 0.14f) - 200f
        }

        val offset = (gameTime - beerSpawnTime) / 2
        return yStartPos - offset * offset * bottleVelocity * 3
    }

    fun getHitbox(gameTime: Float, drawer: SpriteBatch, devMode: Boolean, scale: Int, rotationSystem: MatrixRotation): Hitbox {
        val beerWidth = texture!!.regionWidth * scale
        val beerHeight = texture!!.regionHeight * scale
        val minX = getXOffset(gameTime, scale)
        val minY = getYOffset(gameTime)
        val spinAngle = getSpin(gameTime)
        hitbox.updateHitbox(minX, minY, beerWidth, beerHeight, spinAngle, rotationSystem)

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
                -offset * bottleSpin * 150
            } else bottleSpin * 30
        } else {
            if (!collision) {
                offset * bottleSpin * 150
            } else -bottleSpin * 30
        }
    }

    init {
        texture = getTexture(id, bottlePlayerId, myPlayerId)
        tail = getTail(id, bottlePlayerId, myPlayerId)
        xStartPos = getXStart(id, bottlePlayerId, myPlayerId)
        yStartPos = Gdx.graphics.height - scale * y
        bottleVelocity = velocity * scale
    }
}