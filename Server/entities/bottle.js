class Bottle {
  constructor(id, playerID, offsetY, secondsToSpawn, velocity, spin) {
    this.id = id; //int
    this.playerID = playerID; //string
    this.secondsToSpawn = secondsToSpawn; //float
    this.offsetY = offsetY; //int
    this.velocity = velocity; //int
    this.spin = spin; //float
  }
}

module.exports = {
  Bottle,
};
