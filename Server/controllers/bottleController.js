const { getPlayer, incrementPlayerScore } = require("./playerController");

const { Beer } = require("../models/beer");

const numberOfBeerObjects = 30;

let bottleList = [];
let powerupList = [];

const getBottleList = () => bottleList;

const getPowerupList = () => powerupList;

const isBottleInOpponentsList = (playerID, bottle) => {
  const player = getPlayer(playerID);

  for (let alreadyCaughtBottle of player.bottles) {
    if (alreadyCaughtBottle.id == bottle.id) {
      return true;
    }
  }
  return false;
};

const appendBottle = (playerID, bottle) => {
  const player = getPlayer(playerID);

  player.bottles.push(bottle);
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
      Math.round((i + Math.random()) * 10000) / 10000,
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
      30 + i,
      // seconds to spawn
      Math.round((i / powerupDuration + Math.random()) * 10000) / 10000,
      //offset Y(from top)
      100 + Math.floor(Math.random() * standardOffsetY),
      //Player
      playerID,
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
const setWinningPlayer = (bottle) => {
  const winner = getPlayer(bottle.playerID);

  if (!isBottleInOpponentsList(winner.playerID, bottle)) {
    appendBottle(winner.playerID, bottle);
    incrementPlayerScore(winner.playerID, 1);
  }
};

module.exports = {
  generateListOfBeerObjects,
  getBottleList,
  getPowerupList,
  setWinningPlayer,
};
