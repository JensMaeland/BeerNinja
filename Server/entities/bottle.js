class Bottle {
  constructor(id, secondsToSpawn, offsetY, playerID, velocity, spin) {
    this.id = id; //int
    this.secondsToSpawn = secondsToSpawn; //float
    this.offsetY = offsetY; //int
    this.playerID = playerID; //string
    this.velocity = velocity; //int
    this.spin = spin; //float
  }
}

module.exports = {
  Bottle,
};
