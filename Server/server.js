const {
  generateListOfBeerObjects,
  getWinningPlayerV1,
  getWinningPlayerV2,
  addPlayer,
  allocatePoints,
  createInitialPlayerState,
  pushBottleToCorrectPlayer,
} = require("./eval-functions");

const app = require("express")();
const httpServer = require("http").createServer(app);
const options = {
  /*  */
};
const io = require("socket.io")(httpServer, options);

const players = createInitialPlayerState();

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  addPlayer(socket);
  console.log(players);

  socket.emit("socketID", { id: socket.id });

  socket.on(
    "setUpGame",
    () => players.length === 2 && socket.emit("setUpGame", { id: socket.id })
  );

  socket.on("bottleList", () =>
    socket.emit("bottleList", {
      bottleList: generateListOfBeerObjects(30, players),
    })
  );

  socket.on("caughtBottle", (bottle) => {
    var winner = getWinningPlayerV2(bottle);
    allocatePoints(winner);
    socket.emit("bottleWinner", {
      winningBottle: bottle,
      winningPlayer: winner,
      players: players,
    });
  });

  socket.on("disconnect", function () {
    players = ["2player"];
    console.log("Player Disconnected");
  });
});
