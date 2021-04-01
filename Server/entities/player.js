class Player {
  constructor(id, name, enemyID = "") {
    this.playerID = id;
    this.username = name;
    this.enemyID = enemyID;
    this.score = 0;
    this.bottles = [];
    this.touches = { touches: null, currentTouchIndex: 0 };
  }
}

module.exports = {
  Player,
};
