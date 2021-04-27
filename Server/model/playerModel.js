const { Player } = require("../entities/player");

/* Player Model
 Contains state and methods to manipulate state of all players
 Uses an object containing Player class as values
 Format of players object: {"playerID": player}

 Players are only added here after a game is requested, and deleted after game
 The player object contains players from all concurrent games, and therefore acts singleton-like
*/

const players = {};

const getPlayer = playerID => players[playerID];

const addPlayer = (playerID, username, multiplayer) => {
  const enemy = Object.values(players).filter((p) => p.multiplayer && p.enemyID === "");

  let player;
  if (multiplayer && enemy.length) {
    player = new Player(playerID, username, true, enemy[0].playerID);
    players[playerID] = player;
    enemy[0].enemyID = playerID;
  } else {
    player = new Player(playerID, username, multiplayer);
    players[playerID] = player;
  }

  return player;
};

const removePlayer = playerID => {
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

const resetPlayerScore = playerID => {
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
