const fs = require('fs');

const limit = 40;

const addHighscore = (player1, player2) => {
  if (!player1 && !player2) return;

  fs.readFile('./db/highscore', (err, data) => {
    let list = JSON.parse(data);

    const currentPlayer1 = player1 ? list[player1.username] : 0;
    const currentPlayer2 = player2 ? list[player2.username] : 0;

    if (player1 && player1.score > limit && (!currentPlayer1 || currentPlayer1 < player1.score)) {
      list[player1.username] = player1.score;
    }
    if (player2 && player2.score > limit && (!currentPlayer2 || currentPlayer2 < player2.score)) {
      list[player2.username] = player2.score;
    }

    // sort the new highscore object
    const sorted = Object.entries(list)
      .sort(([, a], [, b]) => b - a)
      .reduce((r, [k, v]) => ({ ...r, [k]: v }), {});

    fs.writeFile('./db/highscore', JSON.stringify(sorted), err => { })
  });
};

const getHighscore = () => {
  const data = fs.readFileSync('./db/highscore');

  const list = JSON.parse(data.toString() || '{}');

  const sliced = Object.keys(list).slice(0, 10).reduce((result, key) => {
    result[key] = list[key];
    return result;
  }, {});

  return sliced;
};

module.exports = {
  addHighscore,
  getHighscore,
};
