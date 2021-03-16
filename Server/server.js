const app = require("express")();
const httpServer = require("http").createServer(app);
const options = {
  /*  */
};
const io = require("socket.io")(httpServer, options);

const users = [];

const red = [];
const blue = [];

class Beer {
  constructor(id, secondsToSpawn, offsetY, player, velocity) {
    this.id = id;
    this.secondsToSpawn = secondsToSpawn;
    this.offsetY = offsetY;
    this.player = player;
    this.velocity = velocity;
  }
}

/*Server now listens to port 8080*/
httpServer.listen(8080, () => {
  console.log("server is now running");
});

io.on("connection", (socket) => {
  console.log("Player connected");
  socket.emit("socketID", { id: socket.id });

  socket.emit("bottleList", { bottleList: generateListOfSprites(30) });

  socket.on("caughtBottle", (bottle) => {});

  socket.on("disconnect", function () {
    console.log("Player Disconnected");
  });
});

const ChooseWinnerByxCoor = (beer1, beer2) => {
  winner = null;
  if (beer1.x < Beer2.x) {
    winner = beer1;
  } else {
    winner = beer2;
  }
};

//(numberOfSprites wants to spawn, id of player)
const generateListOfSprites = (numberOfSprites) => {
  spriteList = [];
  let playerID = 0;
  var playerOne = Math.floor(numberOfSprites / 2);
  var playerTwo = playerOne;

  for (i = 0; i < numberOfSprites; i++) {
    if (Math.random() > 0.5 && playerOne >= 1) {
      playerID = 1;
      playerOne--;
    } else if (playerTwo >= 1) {
      playerID = 2;
      playerTwo--;
    } else {
      playerID = 1;
      playerOne--;
    }

    console.log(playerOne);
    console.log(playerTwo);

    sprite = [];
    //id
    sprite.push(i);

    //seconds to spawn
    sprite.push(i + Math.random());

    //offset Y(from top)
    sprite.push(100 + Math.floor(Math.random() * 800));

    //player
    sprite.push(playerID);

    //sprite velocity
    sprite.push(150 + Math.floor(Math.random() * 250));
    spriteList.push(sprite);
  }

  console.log(playerOne);
  console.log(playerTwo);

  return spriteList;
};
