const {
  addPlayer,
  setPlayerTouches,
  resetPlayerScore,
  removePlayer,
} = require("./controllers/playerController");

const {
  generateListOfBeerObjects,
  getBottleList,
  getPowerupList,
  setWinningPlayer,
} = require("./controllers/bottleController");

const { gameTick } = require("./models/gameTick");

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

  socket.on("setUpGame", (multiplayer) => {
    const gameType = multiplayer ? "multiplayer-game: " : "solo-game: ";
    console.log(gray, "Player requesting new " + gameType + socket.id);

    const player = addPlayer(socket.id);

    if (multiplayer) {
      if (player.enemyID) {
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
        resetPlayerScore(player.enemyID);

        socket.emit("setUpGame", {
          playerID: player.playerID,
          enemyID: player.enemyID,
          bottleList: getBottleList(),
          powerupList: getPowerupList(),
        });
        socket.to(player.enemyID).emit("setUpGame", {
          playerID: player.enemyID,
          enemyID: player.playerID,
          bottleList: getBottleList(),
          powerupList: getPowerupList(),
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
        enemyID: null,
        bottleList: getBottleList(),
        powerupList: getPowerupList(),
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
