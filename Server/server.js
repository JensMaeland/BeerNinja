const {
  generateListOfBeerObjects,
  getWinningPlayerV2,
  addPlayer,
  allocatePoints,
  createInitialPlayerState,
  pushBottleToCorrectPlayer,
  getPlayers,
  setScore,
} = require("./eval-functions");

const app = require("express")();
const httpServer = require("http").createServer(app);
const options = {
  /*  */
};
const io = require("socket.io")(httpServer, options);

// lets user play agains an idle player2
const testmode = true;

isSinglePlayer = false;

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  addPlayer(socket, testmode);
  setScore(getPlayers().player1.playerID, 5);
  setScore(getPlayers().player2.playerID, 8);

  socket.emit("socketID", { id: socket.id });

  socket.on(
    "setUpGame",
    () => {
      const tempPlayers = getPlayers();
      if (tempPlayers.player1 && tempPlayers.player2) {
        const player1ID = tempPlayers.player1.playerID;
        const player2ID = tempPlayers.player2.playerID;

        if (socket.id === player1ID) {
          socket.emit("setUpGame", { playerId: player1ID, enemyId: player2ID })
        }
        else if (socket.id === player2ID) {
          socket.emit("setUpGame", { playerId: player2ID, enemyId: player1ID })
        }
      }
    }
  );

  socket.on("bottleList", () =>
    socket.emit("bottleList", {
      bottleList: generateListOfBeerObjects(30, isSinglePlayer),
    })
  );

  socket.on("caughtBottle", (bottle) => {
    var winner = getWinningPlayerV2(bottle);
    setScore(winner, 1);
    const tempPlayers = getPlayers();
    const player1ID = tempPlayers.player1.playerID;
    const player2ID = tempPlayers.player2.playerID;

    const player1Score = tempPlayers.player1.score;
    const player2Score = tempPlayers.player2.score;

    socket.emit("getPoints", { [player1ID]: player1Score, [player2ID]: player2Score });
  });

  socket.on("disconnect", function (socket) {
    createInitialPlayerState();

    console.log("Player Disconnected");
  });
});
