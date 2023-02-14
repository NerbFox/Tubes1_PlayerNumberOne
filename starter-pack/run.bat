@echo off
:: Game Runner
<<<<<<< HEAD
cd ./starter-bots/JavaBotH1
timeout /t 1
start mvn clean package
timeout /t 5
cd ../..
cd ./runner-publish/
=======
cd ./starter-bots/JavaBotN1/
start mvn clean package 

cd ../../runner-publish/
timeout /t 8
>>>>>>> Nigel
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
<<<<<<< HEAD
cd ../starter-bots/JavaBotH1/target
=======
cd ../starter-bots/JavaBotN1/target
>>>>>>> Nigel
timeout /t 3
start "" java -jar jup.jar
timeout /t 3
<<<<<<< HEAD
cd ../../JavaBot2/target
start "" java -jar nigel.jar
=======

cd ../../JavaBotN2/target
start "" java -jar JavaBot.jar
>>>>>>> Nigel
timeout /t 3

cd ../../JavaBotN3/target
start "" java -jar JavaBot.jar
timeout /t 3

cd ../../../reference-bot-publish
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
cd ../

pause