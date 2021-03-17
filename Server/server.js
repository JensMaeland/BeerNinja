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
  setScore(socket.id, 5);
  console.log(getPlayers());

  socket.emit("socketID", { id: socket.id });

  socket.on(
    "setUpGame",
    () =>
      getPlayers().player1 &&
      getPlayers().player2 &&
      socket.emit("setUpGame", { id: socket.id })
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
    socket.emit("getPoints", {
      players: { [player1ID]: player1Score, [player2ID]: player2Score },
    });
  });

  socket.on("disconnect", function (socket) {
    createInitialPlayerState();

    console.log("Player Disconnected");
  });
});
