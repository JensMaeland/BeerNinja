//(numberOfSprites wants to spawn, id of player)
const players = require("./server");

class Player {
  playerID = "";
  bottles = [];
}

const createInitialPlayerState = () => {
  const player1 = new Player();
  const player2 = new Player();
  const players = {
    player1,
    player2,
  };

  return players;
};

class Beer {
  //   int,     float,        int,      string, int
  constructor(id, secondsToSpawn, offsetY, player, velocity) {
    this.id = id;
    this.secondsToSpawn = secondsToSpawn;
    this.offsetY = offsetY;
    this.player = player;
    this.velocity = velocity;
  }
}

const generateListOfBeerObjects = (numberOfBeerObjects) => {
  var spriteList = new Array(numberOfBeerObjects);
  let playerNumber = 0;
  var playerOne = Math.floor(numberOfBeerObjects / 2);
  var playerTwo = playerOne;

  for (i = 0; i < numberOfBeerObjects; i++) {
    if (Math.random() > 0.5 && playerOne >= 1) {
      playerNumber = 0;
      playerOne--;
    } else if (playerTwo >= 1) {
      playerNumber = 1;
      playerTwo--;
    } else {
      playerNumber = 0;
      playerOne--;
    }
    //playerID = players[playerNumber];
    playerID = playerNumber;
    spriteList[i] = new Beer(
      // id
      i,
      // seconds to spawn
      Math.round((i + Math.random()) * 100) / 100,
      //offset Y(from top)
      100 + Math.floor(Math.random() * 800),
      //Player
      playerID,
      //Sprite Velocity
      150 + Math.floor(Math.random() * 250)
    );
    /*
    beerList.push();

    //id
    sprite.push(i);

    //seconds to spawn
    sprite.push(Math.round((i + Math.random()) * 100) / 100);

    //offset Y(from top)
    sprite.push(100 + Math.floor(Math.random() * 800));

    //player
    sprite.push(playerID);

    //sprite velocity
    sprite.push(150 + Math.floor(Math.random() * 250));
    spriteList.push(sprite);
    */
  }

  console.log(spriteList);
  return spriteList;
};

const addBeerToQueue = (beer) => {};

/*
Denne funksjonen tar inn en flaske. Den sjekker deretter om flasken den fikk inn, eksisterer i motstanderens liste over flasker.
Dersom flasken eksisterer i motstanderens liste over flasker, betyr dette at motstanderen allerede har fått poeng for denne.
Dermed fjernes flasken fra motstanders liste, og funksjonen returnerer playerID til vinneren.

Dersom flasken ikke eksisterer i motstanderens liste over flasker, legges flasken til i spillerens liste over flasker, og 

*/
const chooseWinningPlayer = (bottle) => {
  if (bottle.playerID == players.player1.playerID) {
    for (i = 0; i > players.player2.bottles.length; i++) {
      if (bottle.playerID == players.player2.bottles[i]) {
        const index = players.player2.bottles.indexOf(bottle);
        players.player2.bottles.splice(index, 1);
        return players.player2.playerID;
      }
    }
  } else if (bottle.playerID == players.player2.playerID) {
    for (i = 0; i > players.player1.bottles.length; i++) {
      if (bottle.playerID == players.player1.bottles[i]) {
        const index = players.player1.bottles.indexOf(bottle);
        players.player1.bottles.splice(index, 1);
        return players.player1.playerID;
      }
    }
  }
};

const pushBottleToCorrectPlayer = (bottle) => {
  if (bottle.playerID == players.player1.playerID) {
    players.player1.bottles.push(bottle);
  } else if (bottle.playerID == players.player2.playerID) {
    players.player2.bottles.push(bottle);
  }
};

const addPlayer = (socket) => {
  if (!players.player1.playerID) {
    players.player1.playerID = socket.id;
  } else if (!players.player2.playerID) {
    players.player2.playerID = socket.id;
  }
};

module.exports = {
  generateListOfBeerObjects,
  chooseWinningPlayer,
  addPlayer,
  createInitialPlayerState,
  pushBottleToCorrectPlayer,
};
