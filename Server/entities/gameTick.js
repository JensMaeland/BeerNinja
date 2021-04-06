const { getPlayer, removePlayer } = require("../models/playerModel");
const { Player } = require("./player");

const red = "\x1b[31m%s\x1b[0m";

const gameDuration = 40;

const dtMultiplayer = 1000 / 20;
const dtSolo = 1000 / 5;

const gameTick = (socket, multiplayer = true, timer = 0, enemyID = null) => {
  const player = getPlayer(socket.id);
  const enemy = enemyID
    ? getPlayer(enemyID)
    : multiplayer && player
    ? getPlayer(player.enemyID)
    : null;

  if (timer < gameDuration * 1000 && player && (!multiplayer || enemy)) {
    timer += multiplayer ? dtMultiplayer : dtSolo;

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

        gameTick(
          socket,
          multiplayer,
          timer,
          multiplayer ? player.enemyID : null
        );
      },
      multiplayer ? dtMultiplayer : dtSolo
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

    !player || (multiplayer && !enemy);
    console.log("Game cancelled..");

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

    player && removePlayer(player.playerID);
    multiplayer && enemy && removePlayer(enemy.playerID);
  }
};

module.exports = {
  gameTick,
  gameDuration,
};
