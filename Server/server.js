const {
  addPlayer,
  getPlayer,
  setPlayerTouches,
  resetPlayerScore,
  removePlayer,
} = require("./models/playerModel");

const {
  generateListOfBeerObjects,
  getBottleList,
  getPowerupList,
  setWinningPlayer,
} = require("./models/bottleModel");

const { gameTick, gameDuration } = require("./entities/gameTick");

const app = require("express")();
const httpServer = require("http").createServer(app);
const io = require("socket.io")(httpServer, {});

const red = "\x1b[31m%s\x1b[0m";
const green = "\x1b[32m%s\x1b[0m";
const yellow = "\x1b[33m%s\x1b[0m";
const gray = "\x1b[2m%s\x1b[0m";

const port = 8080;

/*Server now listens to port*/
httpServer.listen(port, () => {
  console.log(yellow, "Tapping fresh beer to start serving..");
});

io.on("connection", (socket) => {
  console.log(green, "Player connecting to server: " + socket.id);
  socket.emit("connection", { connection: true });

  socket.on("setUpGame", (multiplayer, username) => {
    const gameType = multiplayer ? "multiplayer-game: " : "solo-game: ";
    console.log(gray, "Player requesting new " + gameType + socket.id);

    const player = addPlayer(socket.id, username);

    if (multiplayer) {
      if (player.enemyID) {
        const enemy = getPlayer(player.enemyID);

        console.log(
          green,
          "Players: " +
            socket.id +
            " and " +
            player.enemyID +
            " matched up for game.."
        );
        generateListOfBeerObjects(multiplayer, player);

        resetPlayerScore(player.playerID);
        resetPlayerScore(enemy.playerID);

        socket.emit("setUpGame", {
          playerID: player.playerID,
          enemyID: enemy.playerID,
          enemyUsername: enemy.username,
          bottleList: getBottleList(),
          powerupList: getPowerupList(player.playerID),
          gameDuration,
        });
        socket.to(player.enemyID).emit("setUpGame", {
          playerID: enemy.playerID,
          enemyID: player.playerID,
          enemyUsername: player.username,
          bottleList: getBottleList(),
          powerupList: getPowerupList(enemy.playerID),
          gameDuration,
        });

        gameTick(socket);
      } else {
        console.log(gray, "Waiting for player 2: " + socket.id);
      }
    } else {
      console.log(green, "Starting solo game: " + socket.id);
      generateListOfBeerObjects(multiplayer, player);

      resetPlayerScore(player.playerID);
      socket.emit("setUpGame", {
        playerID: player.playerID,
        enemyID: "",
        enemyUsername: "",
        bottleList: getBottleList(),
        powerupList: getPowerupList(player.playerID),
        gameDuration,
      });

      gameTick(socket, false);
    }
  });

  socket.on("touches", (touches) => {
    setPlayerTouches(socket.id, touches);
  });

  socket.on("caughtBottle", (bottleData) => {
    const bottle = JSON.parse(bottleData);

    console.log(
      gray,
      "Caught Bottle: " + bottle.id + " by player " + bottle.playerID
    );
    setWinningPlayer(bottle);
  });

  socket.on("disconnect", () => {
    console.log(red, "Player out of beer: " + socket.id);

    removePlayer(socket.id);
  });
});
