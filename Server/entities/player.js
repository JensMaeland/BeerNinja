class Player {
  constructor(id, name, multiplayer = true, enemyID = "") {
    this.playerID = id;
    this.username = name;
    this.multiplayer = multiplayer;
    this.enemyID = enemyID;
    this.score = 0;
    this.bottles = [];
    this.touches = { touches: null, currentTouchIndex: 0 };
  }
}

module.exports = {
  Player,
};
