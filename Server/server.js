const app = require("express")();
const httpServer = require("http").createServer(app);
const options = {
  /*   */
};
const io = require("socket.io")(httpServer, options);

/*Server now listens to port 8080*/
httpServer.listen(8080, function () {
  console.log("server is now running");
});

io.on("connection", function (socket) {
  console.log("Player connected");

  socket.on("disconnect", function () {
    console.log("Player Disconnected");
  });
});
