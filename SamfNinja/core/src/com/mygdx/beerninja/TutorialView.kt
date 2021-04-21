package com.mygdx.beerninja

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.mygdx.beerninja.Entities.Bottle
import com.mygdx.beerninja.Entities.Touch
import java.util.*

class TutorialView (game: GameView) {
   private var timer = 0f
   private var tutorialBottles: ArrayList<Pair<Bottle, String>>
   private var bottleIndex = 0
   private var cooldown = 1f

   val touches = HashMap<Int, Touch>()
   var enemyTouches = HashMap<Int, Touch>()
   var currentTouchIndex = 0

   var streak = 0
   var points = 0
   private var tailLength = 35

   fun render(menu: MenuView, game: GameView, font: BitmapFont, buttonStartY: Float, buttonMargin: Float, headerY: Float) {
      font.draw(game.drawer, "Tutorial", buttonMargin, headerY)
      font.setColor(1f, 0.7f, 0f, 1f)

      timer += Gdx.graphics.deltaTime
      cooldown -= Gdx.graphics.deltaTime

      if (bottleIndex >= tutorialBottles.size - 1 && cooldown < -5) {
         bottleIndex = 0
         menu.showTutorial = false
      }

      if (cooldown < 0) {
         val bottle = tutorialBottles.get(bottleIndex).first
         val width = (bottle.texture!!.regionWidth * game.scale).toFloat()
         val height = (bottle.texture!!.regionHeight * game.scale).toFloat()

         game.drawer.draw(bottle.texture, (game.screenWidth - width) / 2f, buttonStartY, width / 2, height / 2, width, height, 1f, 1f, bottle.getSpin(timer))
         font.draw(game.drawer, tutorialBottles.get(bottleIndex).second, buttonMargin, game.screenHeight / 4f)

         checkTutorialHitboxes(game, timer, bottle)
      }

      if (bottleIndex >= tutorialBottles.size -1 && cooldown < 0) {
         var visibleIndexes = (tailLength - (cooldown + 3) * 10).toInt()
         if (visibleIndexes > tailLength) {
            visibleIndexes = tailLength
         }

         for (i in 0 until visibleIndexes) {
            val touchX = touches.get(i)?.x
            val touchY = game.screenHeight - touches.get(i)!!.y
            game.drawer.draw(enemyTouches.get(i)?.texture, touchX!!.toFloat(), touchY.toFloat(), 16f * game.scale, 16f * game.scale)
         }
      }

      for (touch in touches.values!!) {
         if (touch.display) {
            val touchX = touch.x
            val touchY = game.screenHeight - touch.y
            game.drawer.draw(touch.texture, touchX.toFloat(), touchY.toFloat(), 16f * game.scale, 16f * game.scale)
         }
      }


      font.setColor(1f, 1f, 1f, 0.5f)
      font.draw(game.drawer, "< Tilbake", buttonMargin, 100f * game.scale)

      Gdx.input.inputProcessor = object : InputAdapter() {
         override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (screenY >= game.screenHeight - 120f * game.scale) {
               bottleIndex = 0
               menu.showTutorial = false
            }
            return true
         }

         override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            if (bottleIndex < tutorialBottles.size -1) {
               val touch = touches[currentTouchIndex]
               currentTouchIndex = (currentTouchIndex + 1) % tailLength

               if (touch != null) {
                  touch.x = screenX
                  touch.y = screenY
                  touch.display = true
               }
            }
            return true
         }

         override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            for (i in 0 until touches.size) {
               val touch = touches[i]
               touch!!.display = false
            }
            return false
         }
      }
   }

   private fun checkTutorialHitboxes(game: GameView, gameTimer: Float, bottle: Bottle) {
      var touch = touches[tailLength - 1]
      if (currentTouchIndex > 0) {
         touch = touches[currentTouchIndex - 1]
      }

      val touchX = touch!!.x
      val touchY = game.screenHeight - touch.y
      val hitbox = bottle.getHitbox(gameTimer, game.drawer, false, game.scale)

      // check touch hits with bottle
      if (touch.display && hitbox.left <= touchX && touchX <= hitbox.right) {
         if (hitbox.bottom <= touchY && touchY <= hitbox.top) {
            cooldown = 2f
            bottleIndex += 1
         }
      }
   }

   init {
      // instancing objects for touch features
      for (i in 0 until tailLength) {
         touches[i] = Touch(i, false, game.textures)
         enemyTouches[i] = Touch(i, true, game.textures)
      }

      tutorialBottles = ArrayList()
      tutorialBottles.add(Pair(Bottle(-1, "myBottle", game.scale * 150, 0, 1f, 0f, game.scale, "myBottle", game.textures), "Dra over grønne flasker.."))
      tutorialBottles.add(Pair(Bottle(-50, "myBottle", game.scale * 150, 0, 1f, 0f, game.scale, "myBottle", game.textures), "Ølglass skal også fanges.."))
      tutorialBottles.add(Pair(Bottle(-1, "myBottle", game.scale * 150, 0, 1f, 0f, game.scale, "myBottle", game.textures), "Alle grønne flasker er dine!"))
      tutorialBottles.add(Pair(Bottle(-420, "powerup", game.scale * 150, 0, 1f, 0f, game.scale, "myBottle", game.textures), "Vær på utkikk etter samfkort!"))
      tutorialBottles.add(Pair(Bottle(-2, "enemyBottle", game.scale * 150, 0, 1f, 0f, game.scale, "myBottle", game.textures), "Hold deg unna blå bokser!"))
   }
}