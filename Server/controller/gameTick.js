const { getPlayer, removePlayer } = require("../model/playerModel");
const { Player } = require("../entities/player");
const { addHighscore } = require("../highscore");

const red = "\x1b[31m%s\x1b[0m";

// game tick rules
const gameDuration = 40;
const multiplayerFrequency = 1000 / 20;
const soloFrequency = 1000 / 5;


/* Game Tick / Game Loop
 Each instance controls a game, responsible for updating clients with a given interval
 Uses state from the two main model-components: Player- and bottle-model

 Socket Structure:

  - Touches (emit to client)
  - Points (emit to client)
  - GameSummary (emit to client)
*/


const gameTick = (socket, multiplayer = true, timer = 0, enemyID = null) => {
  const player = getPlayer(socket.id);
  const enemy = enemyID
    ? getPlayer(enemyID)
    : multiplayer && player
      ? getPlayer(player.enemyID)
      : null;

  if (timer < gameDuration * 1000 && player && (!multiplayer || enemy)) {
    timer += multiplayer ? multiplayerFrequency : soloFrequency;

    setTimeout(
      () => {
        if (enemy) {
          enemy.touches.touches && socket.emit("touches", enemy.touches);
          player.touches.touches &&
            socket.to(enemy.playerID).emit("touches", player.touches);

          socket.emit("points", {
            [player.playerID]: player.score,
            [enemy.playerID]: enemy.score,
            enemyBottles: enemy.bottles,
          });
          socket.to(enemy.playerID).emit("points", {
            [player.playerID]: player.score,
            [enemy.playerID]: enemy.score,
            enemyBottles: player.bottles,
          });
        } else {
          socket.emit("points", {
            [player.playerID]: player.score,
          });
        }

        // move on to next iteration of the gameTick
        gameTick(
          socket,
          multiplayer,
          timer,
          multiplayer ? player.enemyID : null
        );
      },
      multiplayer ? multiplayerFrequency : soloFrequency
    );
  } else {
    multiplayer &&
      player &&
      enemy &&
      console.log(
        red,
        "Game over: " + player.playerID + " and " + enemy.playerID
      );

    !multiplayer && player && console.log(red, "Game over: " + player.playerID);

    if (!player || (multiplayer && !enemy)) {
      console.log("Game cancelled..");
    }

    player &&
      socket.emit("gameSummary", {
        player,
        enemy: enemy || new Player(enemyID, "", socket.id),
      });
    multiplayer &&
      enemy &&
      socket.to(enemy.playerID).emit("gameSummary", {
        enemy: player || new Player(socket.id, "", enemyID),
        player: enemy,
      });

    addHighscore(player, enemy);

    player && removePlayer(player.playerID);
    multiplayer && enemy && removePlayer(enemy.playerID);
  }
};

module.exports = {
  gameTick,
  gameDuration,
};
