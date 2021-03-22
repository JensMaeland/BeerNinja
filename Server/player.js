class Player {
    constructor(id, enemyID = "") {
        this.playerID = id;
        this.enemyID = enemyID;
        this.score = 0;
        this.bottles = [];
    }
};

module.exports = {
    Player
};
