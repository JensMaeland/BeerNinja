const app = require("express");
const httpServer = require("http").createServer(app);
const io = require("socket.io")(httpServer, {});

const colors = {
  red: "\x1b[31m%s\x1b[0m",
  green: "\x1b[32m%s\x1b[0m",
  yellow: "\x1b[33m%s\x1b[0m",
  gray: "\x1b[2m%s\x1b[0m"
};

const {
  addPlayer,
  getPlayer,
  setPlayerTouches,
  resetPlayerScore,
  removePlayer,
} = require("../model/playerModel");

const { GameModel } = require("../model/gameModel");
const { gameTick, gameDuration } = require("./gameTick");
const { getHighscores } = require("../highscore");

/* SERVER Entrypoint
 Main Component of the Server
 Responsible for setting up connections and acting on incoming messages from clients

 Structure:

 - Connection (from client)
    - SetupGame (request by client)
    - Touches (from client)
    - CaughtBottle (from client)
    - Highscore (request by client)
    - Disconnect (from client)
*/

httpServer.listen(8080, () => {
  /*Server now listens for connections*/
  console.log(colors.yellow, "Tapping fresh beer to start serving..");
});

// Object containing all the ongoing games 
const currentGames = {};

// Socket.io Socket FSM
io.on("connection", (socket) => {
  socket.emit("connection", { connection: true });
  console.log(colors.green, "Player connecting to server: " + socket.id);

  socket.on("setUpGame", (multiplayer, username) => setupGame(socket, multiplayer, username));

  socket.on("touches", (touches) => {
    setPlayerTouches(socket.id, touches);
  });

  socket.on("caughtBottle", (bottleData) => {
    const player = getPlayer(socket.id);
    const bottle = JSON.parse(bottleData);

    const gameModel = currentGames[player.gameID];
    gameModel && gameModel.awardPointsForBottle(player.playerID, bottle);

    console.log(
      colors.gray, "Caught Bottle: " + bottle.id + " by player " + bottle.playerID
    );
  });

  socket.on("highscore", () => {
    console.log(colors.yellow, "Sending highscore to" + socket.id);

    const list = getHighscores();
    socket.emit("highscore", list);
  });

  socket.on("disconnect", () => {
    console.log(colors.red, "Player left Samfundet: " + socket.id);

    const player = getPlayer(socket.id);

    if (player) {
      endGame(player.gameID);
      removePlayer(player.playerID);
    }
  });
});

// Function used by the socket after setupGame requested
const setupGame = (socket, multiplayer, username) => {
  const gameType = multiplayer ? "multiplayer-game: " : "solo-game: ";
  console.log(colors.gray, "Player requesting new " + gameType + socket.id);

  let player = getPlayer(socket.id);

  if (!player) {
    player = addPlayer(socket.id, username, multiplayer);
  }

  if (multiplayer && !player.enemyID) {
    console.log(colors.gray, "Waiting for player 2: " + socket.id);
    return;
  }

  const enemy = player.enemyID ? getPlayer(player.enemyID) : null;

  const gameModel = new GameModel(player);
  currentGames[gameModel.id] = gameModel;
  player.gameID = gameModel.id;
  resetPlayerScore(player.playerID);

  if (enemy) {
    enemy.gameID = gameModel.id;
    resetPlayerScore(enemy.playerID);
    console.log(colors.green, "Players starting " + gameType + socket.id +
      " and " + player.enemyID + " matched up..");
  }
  else {
    console.log(colors.green, "Starting " + gameType + socket.id);
  }

  const powerupTimer = gameDuration / 2 + Math.random() * 10;

  socket.emit("setUpGame", {
    playerID: player.playerID,
    enemyID: enemy ? enemy.playerID : "",
    enemyUsername: enemy ? enemy.username : "",
    bottleList: gameModel.bottleList,
    powerupList: gameModel.getPowerupList(player.playerID),
    gameDuration,
    powerupTimer,
  });

  multiplayer && socket.to(player.enemyID).emit("setUpGame", {
    playerID: enemy.playerID,
    enemyID: player.playerID,
    enemyUsername: player.username,
    bottleList: gameModel.bottleList,
    powerupList: gameModel.getPowerupList(enemy.playerID),
    gameDuration,
    powerupTimer,
  });

  // Setting up a gameTick, to frequently update clients on gameState
  gameTick(socket, true);

  console.log(colors.yellow, "Currently " +
    Object.keys(currentGames).length + " concurrent games.");
};

const endGame = id => delete currentGames[id];