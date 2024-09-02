# fantasydiscordbot
Java Spring Boot Project for a Discord bot that works with Yahoo Fantasy Sports API for fantasy football updates

Inspired by https://www.gamedaybot.com/, since it currently does not have support for Yahoo Fantasy Football, I decided to implement it myself!

If you would like to use it as well, you'll first need to go through the OAuth setup for Yahoo APIs. Once that is done, you'll need to use the following VM options to run the project:

* ```-DrefreshToken=<yourrefreshtoken```
* ```-DclientId=<yourclientid>```
* ```-DclientSecret=<yourclientsecret>```
* ```-DleagueId=<yourleagueid>```
* ```-DredirectUri=<yourredirecturi>```

Currently, this assumes that you have previously requested an access token from Yahoo, so you will need the refresh token from a previous attempt. I will add support for an initial token retrieval in the future.

# Project Features
* Java 17
* Maven
* Spring Boot 3.3.3
* Spring Scheduling
* Jackson