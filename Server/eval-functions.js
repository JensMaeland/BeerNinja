//(numberOfSprites wants to spawn, id of player)
class Player {
  playerID = "";
  score = 0;
  bottles = [];
}

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

let players = {};

const getPlayers = () => players;

const createInitialPlayerState = () => {
  const player1 = new Player();
  const player2 = new Player();
  players = {
    player1,
    player2,
  };
};

const generateListOfBeerObjects = (numberOfBeerObjects, isSinglePlayer) => {
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

  return spriteList;
};

/*
Funksjonen tar inn en flaske, og returnerer spilleren som skal få et poeng. 

 */
const getWinningPlayerV2 = (bottle) => {
  if (bottle.playerID == players.player1.playerID) {
    return players.player1;
  } else if (bottle.playerID == players.player2.playerID) {
    return players.player2;
  }
};

const allocatePoints = (player, points) => {
  player.score += points;
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
  allocatePoints,
  getPlayers,
};
