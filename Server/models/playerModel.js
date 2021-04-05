const { Player } = require("../entities/player");

const players = {};

const getPlayer = (playerID) => players[playerID];

const addPlayer = (playerID, username) => {
  const enemy = Object.values(players).filter((p) => p.enemyID === "");

  let player;
  if (enemy.length) {
    player = new Player(playerID, username, enemy[0].playerID);
    players[playerID] = player;
    enemy[0].enemyID = playerID;
  } else {
    player = new Player(playerID, username);
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

const changePlayerScore = (playerID, points = 1) => {
  const player = getPlayer(playerID);
  if (!player) return;

  player.score += points;

  if (player.score < 0) {
    player.score = 0;
  }
};

const resetPlayerScore = (playerID) => {
  const player = getPlayer(playerID);
  if (!player) return;

  player.score = 0;
  player.bottles = [];
};

module.exports = {
  addPlayer,
  setPlayerTouches,
  getPlayer,
  resetPlayerScore,
  removePlayer,
  changePlayerScore,
};
