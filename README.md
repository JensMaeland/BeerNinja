Install and run project :

## Android application

TODO

## Backend (optional, already runs in the cloud)

1. cd into "Server" folder, containing Server.js
2. In terminal, run "npm install"
3. After dependencies are installed, run "npm start" in terminal to start server.
4. Find your local IP, and change the socketUrl constant in SamfNinja/core/src/com/mygdx/beerninja/GameController.kt

"npm start" runs a script defined in package.json, starting the server using Nodemon.
Nodemon is a devtool used when developing node-driven servers, enabling users to not have to restart server once changes are made,
as nodemon restarts server on file saved.
