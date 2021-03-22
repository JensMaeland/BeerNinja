const { Beer } = require("./beer");
const { Player } = require("./player");

const numberOfBeerObjects = 30;

const state = {
  players: {},
  bottleList: []
}

const getPlayers = () => state.players;
const getBottleList = () => state.bottleList;
const getPlayer = playerID => state.players[playerID];

const createInitialState = () => {
  players = {};
  bottleList = [];
};

const addPlayer = playerID => {
  const enemy = Object.values(state.players).filter(p => p.enemyID === "");

  let player;
  if (enemy.length) {
    player = new Player(playerID, enemy[0].playerID);
    state.players[playerID] = player;
    enemy[0].enemyID = playerID;
  }
  else {
    player = new Player(playerID);
    state.players[playerID] = player;
  }

  return player;
};

const isBottleInOpponentsList = (playerID, bottle) => {
  jsonBottle = JSON.parse(bottle);
  const player = getPlayer(playerID);

  for (let alreadyCaughtBottle of player.bottles) {
    if (alreadyCaughtBottle.id == jsonBottle.id) {
      return true;
    }
  }
  return false;
};

const appendBottle = (playerID, bottle) => {
  jsonBottle = JSON.parse(bottle);
  const player = getPlayer(playerID);

  player.bottles.push(jsonBottle);
};

const setScore = (playerID, score) => {
  const player = getPlayer(playerID);
  player.score += score;
};

const generateListOfBeerObjects = (
  isMultiplayer,
  player,
  standardOffsetY = 800,
  standardVelocity = 350
) => {
  var spriteList = new Array(numberOfBeerObjects);
  var playerOne = Math.floor(numberOfBeerObjects / 2);
  console.log("Generating bottles..");

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
  }

  state.bottleList = spriteList;
};

/*
Funksjonen tar inn en flaske, og returnerer spilleren som skal få et poeng. 

 */
const getWinningPlayerV2 = bottle => {
  jsonBottle = JSON.parse(bottle);

  const player = getPlayer(jsonBottle.playerID);
  return player.playerID;
};

const pushBottleToCorrectPlayer = bottle => {
  const player = getPlayer(bottle.playerID);

  player.bottles.push(bottle);
};

module.exports = {
  generateListOfBeerObjects,
  getWinningPlayerV2,
  addPlayer,
  createInitialState,
  pushBottleToCorrectPlayer,
  getPlayers,
  getPlayer,
  getBottleList,
  setScore,
  appendBottle,
  isBottleInOpponentsList,
};
