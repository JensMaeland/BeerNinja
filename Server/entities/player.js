class Player {
  constructor(id, name, multiplayer = true, enemyID = "") {
    this.playerID = id; //string
    this.username = name; //string
    this.multiplayer = multiplayer; //boolean
    this.enemyID = enemyID; //string
    this.score = 0; //int
    this.bottles = [];
    this.touches = { touches: null, currentTouchIndex: 0 };
  }
}

module.exports = {
  Player,
};
