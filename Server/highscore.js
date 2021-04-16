// see player class in entity
const addHighscore = (player1, player2) => {
  //TODO: Create string for both players with Username + Score

  //TODO: Check if highscore txt file exists
  //..Create new file or append strings to end of existing file
  return null;
};

const getHighscore = () => {
  // highscore example: [{Username: "Peder", Score: "42"}, {Username: "Stine", Score: "99"}]
  const highscore = [];

  //TODO: Get the highscore txt file if it exists
  //..Loop through the file and add every line to highscore list
  //..Sort the highscore list from highest to lowest, and slice to only 30? elements
  return highscore;
};

module.exports = {
  addHighscore,
  getHighscore,
};
