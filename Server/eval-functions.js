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
    sprite.push(Math.round((i + Math.random()) * 100) / 100);

    //offset Y(from top)
    sprite.push(100 + Math.floor(Math.random() * 800));

    //player
    sprite.push(playerID);

    //sprite velocity
    sprite.push(150 + Math.floor(Math.random() * 250));
    spriteList.push(sprite);
  }

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
  generateListOfSprites,
};
