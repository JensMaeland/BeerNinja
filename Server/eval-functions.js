//(numberOfSprites wants to spawn, id of player)
const players = require("./server");

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

const chooseWinningPlayer = (beer1, beer2) => {
  if (beer1.x > beer2.x) {
    return beer2;
  } else {
    return beer1;
  }
};

module.exports = {
  generateListOfBeerObjects,
  chooseWinningPlayer,
};
