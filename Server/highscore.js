const fs = require('fs');

const addHighscore = (player1, player2) => {
  if (!player1 && !player2) return;

  //TODO: Create requirement for score, to reduce storage space

  fs.readFile('./db/highscore', (err, data) => {
    let list = JSON.parse(data);

    const currentPlayer1 = player1 ? list[player1.username] : 0;
    const currentPlayer2 = player2 ? list[player2.username] : 0;

    if (player1 && player1.score && (!currentPlayer1 || currentPlayer1 < player1.score)) {
      list[player1.username] = player1.score;
    }
    if (player2 && player2.score && (!currentPlayer2 || currentPlayer2 < player2.score)) {
      list[player2.username] = player2.score;
    }

    fs.writeFile('./db/highscore', JSON.stringify(list), err => { })
  });
};

const getHighscore = () => {
  //TODO: Only return a subset with the highest scores, ascending
  const data = fs.readFileSync('./db/highscore');

  console.log(JSON.parse(data.toString() || '{}'));
  return JSON.parse(data.toString() || '{}');
};

module.exports = {
  addHighscore,
  getHighscore,
};
