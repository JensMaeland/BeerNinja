const {
  generateListOfBeerObjects,
  getWinningPlayerV1,
  getWinningPlayerV2,
  addPlayer,
  allocatePoints,
  createInitialPlayerState,
  pushBottleToCorrectPlayer,
  getPlayers
} = require("./eval-functions");

const app = require("express")();
const httpServer = require("http").createServer(app);
const options = {
  /*  */
};
const io = require("socket.io")(httpServer, options);

// lets user play agains an idle player2
const testmode = true;

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  addPlayer(socket, testmode);

  socket.emit("socketID", { id: socket.id });

  socket.on(
    "setUpGame",
    () => getPlayers().player1 && getPlayers().player2 && socket.emit("setUpGame", { id: socket.id })
  );

  socket.on("bottleList", () =>
    socket.emit("bottleList", {
      bottleList: generateListOfBeerObjects(30),
    })
  );

  socket.on("caughtBottle", (bottle) => {
    var winner = getWinningPlayerV2(bottle);
    allocatePoints(winner);
    socket.emit("bottleWinner", {
      winningBottle: bottle,
      winningPlayer: winner,
      players: getPlayers(),
    });
  });

  socket.on("disconnect", function (socket) {
    createInitialPlayerState();

    console.log("Player Disconnected");
  });
});
