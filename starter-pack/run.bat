@echo off
:: Game Runner
cd ./starter-bots/JavaBotN1/
start mvn clean package 

cd ../../runner-publish/
timeout /t 5
start "" dotnet GameRunner.dll

:: Game Engine
cd ../engine-publish/
timeout /t 1
start "" dotnet Engine.dll

:: Game Logger
cd ../logger-publish/
timeout /t 1
start "" dotnet Logger.dll

:: Bots
cd ../starter-bots/JavaBotN1/target
timeout /t 3
start "" java -jar JavaBot.jar
timeout /t 3
cd ../../JavaBotN/target
start "" java -jar JavaBot.jar
timeout /t 3
cd ../../../reference-bot-publish
start "" dotnet ReferenceBot.dll
cd ../

pause