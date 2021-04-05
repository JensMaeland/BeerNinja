const { getPlayer, changePlayerScore } = require("./playerModel");

const { Beer } = require("../entities/beer");
const { gameDuration } = require("../entities/gameTick");

const numberOfBeerObjects = 50;

let bottleList = [];
let powerupList = [];

const getBottleList = () => bottleList;

const getPowerupList = (playerID) => {
  let powerups = powerupList;
  powerups.forEach((p) => (p.playerID = playerID));

  return powerups;
};

const isBottleInEnemyList = (playerID, bottle) => {
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
        removeBottle(enemy.enemyID);

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

const appendBottle = (playerID, bottle) => {
  const player = getPlayer(playerID);

  player.bottles.push(bottle);
};

const removeBottle = (playerID) => {
  const player = getPlayer(playerID);

  delete player.bottles[playerID];
};

const generateListOfBeerObjects = (
  isMultiplayer,
  player,
  standardOffsetY = 800,
  standardVelocity = 350
) => {
  console.log("Generating bottles..");

  const powerupDuration = 6;
  var playerOne = Math.floor(numberOfBeerObjects / 2);

  var spriteList = new Array(numberOfBeerObjects);
  var powerupSpriteList = new Array(numberOfBeerObjects);

  let playerID;

  for (i = 0; i < numberOfBeerObjects; i++) {
    if (!isMultiplayer) {
      playerID = player.playerID;
    } else if (Math.random() > 0.5 && playerOne) {
      playerID = player.playerID;
      playerOne--;
    } else {
      playerID = player.enemyID;
    }

    //Creates new beer object
    spriteList[i] = new Beer(
      // id
      i,
      // seconds to spawn
      Math.round(
        (i * ((gameDuration - 5) / numberOfBeerObjects) + Math.random()) * 10000
      ) / 10000,
      //offset Y(from top)
      100 + Math.floor(Math.random() * standardOffsetY),
      //Player
      playerID,
      //Sprite Velocity
      standardVelocity + Math.floor(Math.random() * 250),
      Math.PI * Math.random()
    );

    //Creates new powerup beer object
    powerupSpriteList[i] = new Beer(
      // id
      numberOfBeerObjects + i,
      // seconds to spawn
      Math.round((i / powerupDuration + Math.random()) * 10000) / 10000,
      //offset Y(from top)
      100 + Math.floor(Math.random() * standardOffsetY),
      //Player
      "",
      //Sprite Velocity
      standardVelocity + Math.floor(Math.random() * 150),
      Math.PI * Math.random()
    );
  }

  bottleList = spriteList;
  powerupList = powerupSpriteList;
};

/*
Funksjonen tar inn en flaske, og finner spilleren som skal få et poeng. 

 */
const awardPointsForBottle = (playerID, bottle) => {
  const player = getPlayer(playerID);
  const bottleOwner = getPlayer(bottle.playerID);

  if (!player || !bottleOwner) return;

  if (!isBottleInEnemyList(player.playerID, bottle)) {
    appendBottle(player.playerID, bottle);
    if (player.playerID === bottleOwner.playerID) {
      changePlayerScore(player.playerID, bottle.points);
    } else {
      changePlayerScore(player.playerID, -bottle.points);
    }
  }
};

module.exports = {
  generateListOfBeerObjects,
  getBottleList,
  getPowerupList,
  awardPointsForBottle,
};
