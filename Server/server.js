const {
  generateListOfBeerObjects,
  chooseWinningPlayer,
  addPlayer,
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

console.log(players);

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  addPlayer(socket);
  console.log(players);

  socket.emit("socketID", { id: socket.id });

  socket.emit("bottleList", {
    bottleList: generateListOfBeerObjects(30),
  });

  socket.on("caughtBottle", (bottle) => {
    if (bottle.id == players.player1) {
    } else {
    }
  });

  socket.on("disconnect", function () {
    console.log("Player Disconnected");
  });
});

const findMatchingBottles = (red, blue) => {
  for (i = 0; i < len(red); i++) {}
};

module.exports = players;
