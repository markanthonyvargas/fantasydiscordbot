# fantasydiscordbot
Java Spring Boot Project for a Discord bot that works with Yahoo Fantasy Sports API for fantasy football updates

Inspired by https://www.gamedaybot.com/, since it currently does not have support for Yahoo Fantasy Football, I decided to implement it myself!

If you would like to use it as well, you'll first need to go through the OAuth setup for Yahoo APIs. Once that is done, you'll need to set the following environment variables to run the project:

* ```REFRESH_TOKEN=<yourrefreshtoken```
* ```CLIENT_ID=<yourclientid>```
* ```CLIENT_SECRET=<yourclientsecret>```
* ```LEAGUE_ID=<yourleagueid>```
* ```REDIRECT_URI=<yourredirecturi>```

Currently, this assumes that you have previously requested an access token from Yahoo, so you will need the refresh token from a previous attempt. I will add support for an initial token retrieval in the future.

# Project Features
* Java 17
* Maven
* Spring Boot 3.3.3
* Spring Scheduling
* Jackson