const { getPlayer, changePlayerScore } = require("./playerModel");

const { Bottle } = require("../entities/bottle");
const { gameDuration } = require("../controller/gameTick");

// game rules
const numberOfBeerObjects = 50;
const powerupDuration = 6;


/* Bottle Model
 Contains state and methods to manipulate state of the bottles (sprites) for a given game
 Uses lists of the Bottle class as values

 The bottle model only represents a single game, so multiple instances can run concurrently

*/

class GameModel {
  constructor(player) {
    this.bottleList = [];
    this.powerupList = [];
    this.id = player.playerID.split("").reverse().join("");
    this.generateListOfBottles(player);
  }

  getPowerupList = playerID => {
    let powerups = this.powerupList;
    powerups.forEach((p) => (p.playerID = playerID));

    return powerups;
  };

  isBottleInEnemyList = (playerID, bottle) => {
    const player = getPlayer(playerID);
    if (!player) return false;

    const enemy = getPlayer(player.enemyID);
    if (!enemy) return false;

    for (let alreadyCaughtBottle of enemy.bottles) {
      if (alreadyCaughtBottle.id == bottle.id) {
        // enemy has caught bottle before the player
        if (alreadyCaughtBottle.time <= bottle.time) {
          return true;
        } else {
          // enemy has caught bottle after the player
          this.removeBottle(enemy.enemyID);

          if (enemy.playerID === bottle.playerID) {
            changePlayerScore(enemy.enemyID, -bottle.points);
          } else {
            changePlayerScore(enemy.playerID, bottle.points);
          }
          return false;
        }
      }
    }
    return false;
  };

  generateListOfBottles = (
    player,
    standardOffsetY = 800,
    standardVelocity = 350
  ) => {
    console.log("Generating bottles..");

    let spriteList = new Array(numberOfBeerObjects);
    let powerupSpriteList = new Array(numberOfBeerObjects);

    let playerID, playerOne = Math.floor(numberOfBeerObjects / 2);

    for (let i = 0; i < numberOfBeerObjects; i++) {
      if (!player.enemyID || Math.random() > 0.5 && playerOne) {
        playerID = player.playerID;
        playerOne--;
      } else {
        playerID = player.enemyID;
      }

      const spawnTime = Math.round(
        (i * ((gameDuration - 5) / numberOfBeerObjects) + Math.random()) * 10000
      ) / 10000;

      const powerupSpawnTime = Math.round((i / powerupDuration + Math.random()) * 10000) / 10000;

      //Creates new beer object
      spriteList[i] = new Bottle(
        i, playerID, spawnTime,
        100 + Math.floor(Math.random() * standardOffsetY),
        standardVelocity + Math.floor(Math.random() * 250),
        Math.PI * Math.random()
      );

      //Creates new powerup beer object
      powerupSpriteList[i] = new Bottle(
        numberOfBeerObjects + i, "", powerupSpawnTime,
        100 + Math.floor(Math.random() * standardOffsetY),
        standardVelocity + Math.floor(Math.random() * 150),
        Math.PI * Math.random()
      );
    }

    this.bottleList = spriteList;
    this.powerupList = powerupSpriteList;
  };

  /*
  Funksjonen tar inn en flaske, og finner spilleren som skal få et poeng. 
  */
  awardPointsForBottle = (playerID, bottle) => {
    const player = getPlayer(playerID);
    const bottleOwner = getPlayer(bottle.playerID);

    if (!player || !bottleOwner) return;

    if (!this.isBottleInEnemyList(player.playerID, bottle)) {
      this.appendBottle(player.playerID, bottle);
      if (player.playerID === bottleOwner.playerID) {
        changePlayerScore(player.playerID, bottle.points);
      } else {
        changePlayerScore(player.playerID, -bottle.points);
      }
    }
  };

  appendBottle = (playerID, bottle) => {
    const player = getPlayer(playerID);
    if (!player) return;

    player.bottles.push(bottle);
  };

  removeBottle = playerID => {
    const player = getPlayer(playerID);
    if (!player) return;

    delete player.bottles[playerID];
  };
};

module.exports = {
  GameModel
};
