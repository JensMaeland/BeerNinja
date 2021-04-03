const { getPlayer, removePlayer } = require("../models/playerModel");

const red = "\x1b[31m%s\x1b[0m";

const gameDuration = 40;

const dtMultiplayer = 1000 / 20;
const dtSolo = 1000 / 5;

const gameTick = (socket, multiplayer = true, timer = 0) => {
  if (timer < gameDuration * 1000) {
    timer += multiplayer ? dtMultiplayer : dtSolo;

    setTimeout(
      () => {
        const player = getPlayer(socket.id);
        if (!player) return;

        const enemy = multiplayer && getPlayer(player.enemyID);
        if (multiplayer && !enemy) return;

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

        gameTick(socket, multiplayer, timer);
      },
      multiplayer ? dtMultiplayer : dtSolo
    );
  } else {
    const player = getPlayer(socket.id);
    const enemy = multiplayer && player && getPlayer(player.enemyID);

    multiplayer
      ? console.log(
          red,
          "Game over: " + player.playerID + " and " + player.enemyID
        )
      : console.log(red, "Game over: " + player.playerID);

    player && socket.emit("gameSummary", { player, enemy });
    multiplayer &&
      enemy &&
      socket
        .to(enemy.playerID)
        .emit("gameSummary", { enemy: player, player: enemy });

    removePlayer(player.playerID);
    multiplayer && removePlayer(enemy.playerID);
  }
};

module.exports = {
  gameTick,
  gameDuration,
};
