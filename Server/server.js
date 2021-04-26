const {
  addPlayer,
  getPlayer,
  setPlayerTouches,
  resetPlayerScore,
  removePlayer,
} = require("./models/playerModel");

const { BottleModel } = require("./models/bottleModel");

const { gameTick, gameDuration } = require("./gameTick");

const { getHighscore } = require("./highscore");

const app = require("express");
const httpServer = require("http").createServer(app);
const io = require("socket.io")(httpServer, {});

const red = "\x1b[31m%s\x1b[0m";
const green = "\x1b[32m%s\x1b[0m";
const yellow = "\x1b[33m%s\x1b[0m";
const gray = "\x1b[2m%s\x1b[0m";

const port = 8080;


/* SERVER Entrypoint / Socket FSM
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

/*Server now listens to port*/
httpServer.listen(port, () => {
  console.log(yellow, "Tapping fresh beer to start serving..");
});

const currentGames = {};

const endGame = id => delete currentGames[id];

io.on("connection", (socket) => {
  console.log(green, "Player connecting to server: " + socket.id);
  socket.emit("connection", { connection: true });

  socket.on("setUpGame", (multiplayer, username) => {
    const gameType = multiplayer ? "multiplayer-game: " : "solo-game: ";
    console.log(gray, "Player requesting new " + gameType + socket.id);

    let player = getPlayer(socket.id);

    if (!player) {
      player = addPlayer(socket.id, username, multiplayer);
    }

    if (multiplayer) {
      if (player.enemyID) {
        const enemy = getPlayer(player.enemyID);

        const bottleState = new BottleModel(player);
        player.gameID = bottleState.id;
        enemy.gameID = bottleState.id;
        currentGames[bottleState.id] = bottleState;


        console.log(
          green,
          "Players: " +
          socket.id +
          " and " +
          player.enemyID +
          " matched up for game.."
        );

        resetPlayerScore(player.playerID);
        resetPlayerScore(enemy.playerID);

        const powerupTimer = gameDuration / 2 + Math.random() * 10;

        socket.emit("setUpGame", {
          playerID: player.playerID,
          enemyID: enemy.playerID,
          enemyUsername: enemy.username,
          bottleList: bottleState.bottleList,
          powerupList: bottleState.getPowerupList(player.playerID),
          gameDuration,
          powerupTimer,
        });
        socket.to(player.enemyID).emit("setUpGame", {
          playerID: enemy.playerID,
          enemyID: player.playerID,
          enemyUsername: player.username,
          bottleList: bottleState.bottleList,
          powerupList: bottleState.getPowerupList(enemy.playerID),
          gameDuration,
          powerupTimer,
        });

        // Setting up a gameTick, to frequently update clients on gameState
        gameTick(socket, true);
      } else {
        console.log(gray, "Waiting for player 2: " + socket.id);
      }
    } else {
      console.log(green, "Starting solo game: " + socket.id);

      const bottleState = new BottleModel(player);
      player.gameID = bottleState.id;
      currentGames[bottleState.id] = bottleState;

      const powerupTimer = gameDuration / 2 + Math.random() * 10;

      resetPlayerScore(player.playerID);

      socket.emit("setUpGame", {
        playerID: player.playerID,
        enemyID: "",
        enemyUsername: "",
        bottleList: bottleState.bottleList,
        powerupList: bottleState.getPowerupList(player.playerID),
        gameDuration,
        powerupTimer,
      });

      // Setting up a gameTick, to frequently update clients on gameState
      gameTick(socket, false);
    }

    console.log(
      yellow,
      "Currently " +
      Object.keys(currentGames).length +
      " concurrent games."
    );
  });

  socket.on("touches", (touches) => {
    setPlayerTouches(socket.id, touches);
  });

  socket.on("caughtBottle", (bottleData) => {
    const player = getPlayer(socket.id);
    const bottle = JSON.parse(bottleData);

    console.log(
      gray,
      "Caught Bottle: " + bottle.id + " by player " + bottle.playerID
    );

    const bottleState = currentGames[player.gameID];
    bottleState.awardPointsForBottle(player.playerID, bottle);
  });

  socket.on("highscore", () => {
    const list = getHighscore();
    socket.emit("highscore", list);

    console.log(yellow, "Sending highscore to" + socket.id);
  });

  socket.on("disconnect", () => {
    console.log(red, "Player out of beer: " + socket.id);

    const player = getPlayer(socket.id);

    if (player) {
      endGame(player.gameID);
      removePlayer(player.playerID);
    }
  });
});
