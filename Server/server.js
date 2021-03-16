const { generateListOfSprites } = require("./eval-functions");
const app = require("express")();
const httpServer = require("http").createServer(app);
const options = {
  /*  */
};
const io = require("socket.io")(httpServer, options);

const users = [];

const red = [];
const blue = [];

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  console.log("Player connected");
  socket.emit("socketID", { id: socket.id });

  socket.emit("bottleList", {
    bottleList: generateListOfSprites(30),
  });

  socket.on("caughtBottle", (bottle) => {});

  socket.on("disconnect", function () {
    console.log("Player Disconnected");
  });
});

console.log(generateListOfSprites(30));
