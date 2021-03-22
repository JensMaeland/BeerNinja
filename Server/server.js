const {
  generateListOfBeerObjects,
  getWinningPlayerV2,
  addPlayer,
  isBottleInOpponentsList,
  allocatePoints,
  appendBottle,
  createInitialState,
  pushBottleToCorrectPlayer,
  getBottleList,
  getPlayers,
  getPlayer,
  setScore,
} = require("./eval-functions");

const app = require("express")();
const httpServer = require("http").createServer(app);
const io = require("socket.io")(httpServer, {});

const port = 8080;

/*Server now listens to port 8080*/
httpServer.listen(port, () => {
  console.log("Server up and running..");
});

io.on("connection", (socket) => {
  console.log("Player connected: " + socket.id);
  //socket.emit("socketID", { id: socket.id });

  socket.on("setUpGame", (multiplayer) => {
    // add player to game
    const player = addPlayer(socket.id);

    let players = getPlayers();
    console.log(players);

    if (player.enemyID) {
      generateListOfBeerObjects(multiplayer, player);
    }

    console.log("Requested game: " + socket.id);

    if (multiplayer) {
      if (player.enemyID) {
        console.log("Starting game")

        setScore(player.playerID, 0);
        setScore(player.enemyID, 0);

        socket.emit("setUpGame", { playerID: player.playerID, enemyID: player.enemyID });
        socket.to(player.enemyID).emit("setUpGame", { playerID: player.enemyID, enemyID: player.playerID });
      }
      else {
        console.log("Waiting for player 2..")
      }
    }
    else {
      console.log("Starting solo game");

      setScore(player.playerID, 0);
      socket.emit("setUpGame", { playerID: player.playerID, enemyID: null });
    }
  });

  socket.on("touches", (touches) => {
    let player = getPlayer(socket.id);

    io.to(player.enemyID).emit("touches", touches);
  });

  socket.on("bottleList", () => {
    socket.emit("bottleList", {
      bottleList: getBottleList(),
    })
  }
  );

  socket.on("caughtBottle", (bottle) => {
    console.log("Caught Bottle : ", bottle);
    let player = getPlayer(socket.id);

    // Returnerer bottle.playerID
    var winner = getWinningPlayerV2(bottle);
    console.log("Winner: ", winner);

    if (!isBottleInOpponentsList(winner, bottle)) {
      appendBottle(winner, bottle);
      setScore(winner, 1);
    }

    player = getPlayer(player.playerID);
    const enemy = getPlayer(player.enemyID);

    socket.emit("getPoints", {
      [player.playerID]: player.score,
      [enemy.playerID]: enemy.score,
    });
  });

  socket.on("disconnect", function (socket) {
    createInitialState();

    console.log("Player Disconnected");
  });
});
