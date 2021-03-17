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

let players = ["2player"];

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  console.log("Player Connected: " + socket.id);
  players.push(socket.id);

  socket.on("setUpGame", () => players.length === 2 && socket.emit("setUpGame", { id: socket.id }));

  socket.on("bottleList", () => socket.emit("bottleList", {
    bottleList: generateListOfBeerObjects(30, players),
  }));

  socket.on("caughtBottle", (bottle) => { console.log(bottle) });

  socket.on("disconnect", function () {
    players = ["2player"];
    console.log("Player Disconnected");
  });
});
