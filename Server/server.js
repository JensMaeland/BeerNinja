const {
  generateListOfBeerObjects,
  getWinningPlayerV2,
  addPlayer,
  isBottleInOpponentsList,
  allocatePoints,
  appendBottle,
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
let generatedBottleList = [];

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  addPlayer(socket, testmode);

  socket.emit("socketID", { id: socket.id });

  socket.on("setUpGame", () => {
    generatedBottlelist = generateListOfBeerObjects(30, isSinglePlayer);
    let tempPlayers = getPlayers();
    if (tempPlayers.player1 && tempPlayers.player2) {
      const player1ID = tempPlayers.player1.playerID;
      const player2ID = tempPlayers.player2.playerID;

      if (socket.id === player1ID) {
        socket.emit("setUpGame", { playerID: player1ID, enemyID: player2ID });
      } else if (socket.id === player2ID) {
        socket.emit("setUpGame", { playerID: player2ID, enemyID: player1ID });
      }
    }
  });
  socket.on("touches", (touches) => {
    let tempPlayers = getPlayers();
    if (socket.id == tempPlayers.player1.playerID) {
      io.to(tempPlayers.player2.playerID).emit("touches", { touches: touches });
    } else if (socket.id == tempPlayers.player2.playerID) {
      io.to(tempPlayers.player1.playerID).emit("touches", { touches: touches });
    }
  });

  socket.on("bottleList", () =>
    socket.emit("bottleList", {
      bottleList: generatedBottlelist,
    })
  );

  socket.on("caughtBottle", (bottle) => {
    console.log("Caught Bottle : ", bottle);
    let tempPlayers = getPlayers();

    // Returnerer bottle.playerID
    var winner = getWinningPlayerV2(bottle);
    console.log("Winner: ", winner);
    if (winner == tempPlayers.player1.playerID) {
      if (!isBottleInOpponentsList(tempPlayers.player1.playerID, bottle)) {
        appendBottle(tempPlayers.player1.playerID, bottle);
        tempPlayers = setScore(winner, 1);
      }
    } else if (winner == tempPlayers.player2.playerID) {
      if (!isBottleInOpponentsList(tempPlayers.player2.playerID, bottle)) {
        appendBottle(tempPlayers.player2.playerID, bottle);
        tempPlayers = setScore(winner, 1);
      }
    }

    //console.log("Updated Players :", tempPlayers);

    const player1ID = tempPlayers.player1.playerID;
    const player2ID = tempPlayers.player2.playerID;
    const player1Score = tempPlayers.player1.score;
    const player2Score = tempPlayers.player2.score;
    //console.log(tempPlayers);
    /*
    console.log(player1ID);
    console.log(player2ID);
    console.log(player1Score);
    console.log(player1Score);
*/
    socket.emit("getPoints", {
      [player1ID]: player1Score,
      [player2ID]: player2Score,
    });
  });

  socket.on("disconnect", function (socket) {
    createInitialPlayerState();

    console.log("Player Disconnected");
  });
});
