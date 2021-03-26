const { Player } = require("../models/player");

const players = {};

const getPlayer = (playerID) => players[playerID];

const addPlayer = (playerID) => {
  const enemy = Object.values(players).filter((p) => p.enemyID === "");

  let player;
  if (enemy.length) {
    player = new Player(playerID, enemy[0].playerID);
    players[playerID] = player;
    enemy[0].enemyID = playerID;
  } else {
    player = new Player(playerID);
    players[playerID] = player;
  }

  return player;
};

const removePlayer = (playerID) => {
  delete players[playerID];
};

const setPlayerTouches = (playerID, touchData) => {
  const player = getPlayer(playerID);
  if (!player) return;

  player.touches.touches = JSON.parse(touchData.touches);
  player.touches.currentTouchIndex = touchData.currentTouchIndex;
};

const incrementPlayerScore = (playerID) => {
  const player = getPlayer(playerID);
  if (!player) return;

  player.score += 1;
};

const resetPlayerScore = (playerID) => {
  const player = getPlayer(playerID);
  if (!player) return;

  player.score = 0;
};

module.exports = {
  addPlayer,
  setPlayerTouches,
  getPlayer,
  resetPlayerScore,
  removePlayer,
  incrementPlayerScore,
};
