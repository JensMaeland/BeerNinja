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

const generateListOfBeerObjects = (numberOfBeerObjects) => {
  var spriteList = new Array(numberOfBeerObjects);
  var playerOne = Math.floor(numberOfBeerObjects / 2);
  console.log("Generating bottles..");

  for (i = 0; i < numberOfBeerObjects; i++) {
    if (Math.random() > 0.5 && playerOne >= 1) {
      playerID = players.player1.playerID;
      playerOne--;
    } else {
      playerID = players.player2.playerID;
    }

    playerID;
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

const addBeerToQueue = (beer) => { };

/*
Denne funksjonen tar inn en flaske. Den sjekker deretter om flasken den fikk inn, eksisterer i motstanderens liste over flasker.
Dersom flasken eksisterer i motstanderens liste over flasker, betyr dette at motstanderen allerede har fått poeng for denne.
Dermed fjernes flasken fra motstanders liste, og funksjonen returnerer playerID til vinneren.

Dersom flasken ikke eksisterer i motstanderens liste over flasker, legges flasken til i spillerens liste over flasker, og ingenting skjer
Problemet med dette er at det ikke vil bli noen som helst vinner dersom første spiller plukker opp flasken, men spiller 2 ikke tar flasken.
Denne ble laget mtp gamemode hvor begge spiller om samme flaske.
*/
const getWinningPlayerV1 = (bottle) => {
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

const allocatePoints = (player, points) => {
  player.points += points;
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

    if (testMode) players.player2.playerID = 'testplayer_2';

  } else if (!players.player2.playerID) {
    players.player2.playerID = socket.id;
  }
};

module.exports = {
  generateListOfBeerObjects,
  getWinningPlayerV1,
  getWinningPlayerV2,
  addPlayer,
  createInitialPlayerState,
  pushBottleToCorrectPlayer,
  allocatePoints,
  getPlayers
};
