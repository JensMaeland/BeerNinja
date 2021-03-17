//(numberOfSprites wants to spawn, id of player)
class Player {
  playerID = "";
  score = 0;
  bottles = [];
}

class Beer {
  //   int,     float,        int,      string,       int, float
  constructor(id, secondsToSpawn, offsetY, playerID, velocity, spin) {
    this.id = id;
    this.secondsToSpawn = secondsToSpawn;
    this.offsetY = offsetY;
    this.playerID = playerID;
    this.velocity = velocity;
    this.spin = spin;
  }
}

let players = {};

const getPlayers = () => players;

const setScore = (playerID, score) => {
  if (players.player1.playerID == playerID) {
    players.player1.score += score;
  } else if (players.player2.playerID == playerID) {
    players.player2.score += score;
  }
};

const createInitialPlayerState = () => {
  const player1 = new Player();
  const player2 = new Player();
  players = {
    player1,
    player2,
  };
};

const generateListOfBeerObjects = (
  numberOfBeerObjects,
  isSinglePlayer,
  standardOffsetY = 800,
  standardVelocity = 350
) => {
  var spriteList = new Array(numberOfBeerObjects);
  var playerOne = Math.floor(numberOfBeerObjects / 2);
  console.log("Generating bottles..");

  for (i = 0; i < numberOfBeerObjects; i++) {
    if (isSinglePlayer) {
      playerID = players.player1.playerID;
    } else if (Math.random() > 0.5 && playerOne >= 1) {
      playerID = players.player1.playerID;
      playerOne--;
    } else {
      playerID = players.player2.playerID;
    }

    //Creates new beer object
    spriteList[i] = new Beer(
      // id
      i,
      // seconds to spawn
      Math.round((i + Math.random()) * 10000) / 10000,
      //offset Y(from top)
      100 + Math.floor(Math.random() * standardOffsetY),
      //Player
      playerID,
      //Sprite Velocity
      standardVelocity + Math.floor(Math.random() * 250),
      Math.PI * Math.random()
    );
  }

  return spriteList;
};

/*
Funksjonen tar inn en flaske, og returnerer spilleren som skal få et poeng. 

 */
const getWinningPlayerV2 = (bottle) => {
  if (bottle.playerID == players.player1.playerID) {
    return players.player1.playerID;
  } else if (bottle.playerID == players.player2.playerID) {
    return players.player2.playerID;
  }
};

const pushBottleToCorrectPlayer = (bottle) => {
  if (bottle.playerID == players.player1.playerID) {
    players.player1.bottles.push(bottle);
  } else if (bottle.playerID == players.player2.playerID) {
    players.player2.bottles.push(bottle);
  }
};

const addPlayer = (socket, testMode = false) => {
  if (!players.player1 && !players.player2) {
    createInitialPlayerState();
  }

  if (!players.player1.playerID) {
    players.player1.playerID = socket.id;

    if (testMode) players.player2.playerID = "testplayer_2";
  } else if (!players.player2.playerID) {
    players.player2.playerID = socket.id;
  }
};

module.exports = {
  generateListOfBeerObjects,
  getWinningPlayerV2,
  addPlayer,
  createInitialPlayerState,
  pushBottleToCorrectPlayer,
  getPlayers,
  setScore,
};
