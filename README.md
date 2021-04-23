How to install and run project :

## Download the project
1. Clone the repository from GitLab. %kegg ved lenken

## Client (Android application)

Prerequisites that need to be downloaded and installed:

* [Android Studio](https://developer.android.com/studio?gclid=CjwKCAjwmv-DBhAMEiwA7xYrd9KlZJxeXAA8sPACxZlKsYZ7w5u7hUIhCVkXyv9kBoSiQOoaoCxELhoC5W4QAvD_BwE&gclsrc=aw.ds)
* [libGDX](https://libgdx.com/dev/setup/)
* [Setup of Android Studio, libGDX and Android SDK. See pages 3-9.]({https://learn-eu-central-1-prod-fleet01-xythos.content.blackboardcdn.com/5def77a38a2f7/3312051?X-Blackboard-Expiration=1619211600000&X-Blackboard-Signature=hw0GTy0hyiAfy9ETeyooOpJnr71nX8dRPBKM\%2BEkWluw\%3D&X-Blackboard-Client-Id=303508&response-cache-control=private\%2C\%20max-age\%3D21600&response-content-disposition=inline\%3B\%20filename\%2A\%3DUTF-8\%27\%27Tech_Introduction_Exercise\%2520version\%25202020.pdf&response-content-type=application\%2Fpdf&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20210423T150000Z&X-Amz-SignedHeaders=host&X-Amz-Expires=21600&X-Amz-Credential=AKIAZH6WM4PL5M5HI5WH\%2F20210423\%2Feu-central-1\%2Fs3\%2Faws4_request&X-Amz-Signature=f6055fe197faf9215ed11f9f6c1cd0e907a1078767db8cf8b112b3aff100004e)


1. Navigate to the folder "SamfNinja" and open it in Android Studio. 
2. If this is the first time using Android Studio, you need to configure the emulator. If already done, move on to the next step. To configure the Android emulator: 

        2.1 Press "Add Configuration". 
        2.2 Press "+" and "Android App". 
        2.3 Fill in the missing information, as seen in \autoref{fig:emulator}.

3. Press the green triangle in the top menu to run the client with Android emulator.

## Server (optional, already runs in the cloud)

1. Open the root folder "BeerNinja" in a terminal
2. cd into the "Server" folder, containing Server.js
3. In terminal, run "npm install"
4. After dependencies are installed, run "npm start" in terminal to start server.
5. Find your local IP, and change the socketUrl constant in SamfNinja/core/src/com/mygdx/beerninja/GameController.kt

"npm start" runs a script defined in package.json, starting the server using Nodemon.
Nodemon is a devtool used when developing node-driven servers, enabling users to not have to restart server once changes are made,
as nodemon restarts server on file saved.
