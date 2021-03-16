const {
  generateListOfBeerObjects,
  chooseWinningPlayer,
} = require("./eval-functions");
const app = require("express")();
const httpServer = require("http").createServer(app);
const options = {
  /*  */
};
const io = require("socket.io")(httpServer, options);

const players = [];

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  players.push(socket.id);
  socket.emit("socketID", { id: socket.id });

  socket.emit("bottleList", {
    bottleList: generateListOfBeerObjects(30),
  });

  socket.on("caughtBottle", (bottle) => {});

  socket.on("disconnect", function () {
    console.log("Player Disconnected");
  });
});

console.log(generateListOfBeerObjects(30));

module.exports = players;
