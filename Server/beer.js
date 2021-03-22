class Beer {
    //          int, float,         int,     string,   int,      float
    constructor(id, secondsToSpawn, offsetY, playerID, velocity, spin) {
        this.id = id;
        this.secondsToSpawn = secondsToSpawn;
        this.offsetY = offsetY;
        this.playerID = playerID;
        this.velocity = velocity;
        this.spin = spin;
    }
};

module.exports = {
    Beer
};
