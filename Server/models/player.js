class Player {
  constructor(id, enemyID = "") {
    this.playerID = id;
    this.enemyID = enemyID;
    this.score = 0;
    this.bottles = [];
    this.touches = { touches: null, currentTouchIndex: 0 };
  }
}

module.exports = {
  Player,
};
