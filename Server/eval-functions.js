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

const generateListOfBeerObjects = (numberOfBeerObjects, players) => {
  var spriteList = new Array(numberOfBeerObjects);
  var playerOne = Math.floor(numberOfBeerObjects / 2);
  console.log("Generating bottles..")

  for (i = 0; i < numberOfBeerObjects; i++) {
    if (Math.random() > 0.5 && playerOne >= 1) {
      playerID = players[0];
      playerOne--;
    } else {
      playerID = players[1];
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
