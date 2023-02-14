@echo off
:: Game Runner
cd ./starter-bots/JavaBotH1
timeout /t 1
start mvn clean package
timeout /t 5
cd ../..
cd ./runner-publish/
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
cd ../starter-bots/JavaBotH1/target
timeout /t 3
start "" java -jar JavaBot.jar
cd ../../JavaBotN1/target
timeout /t 3
start "" java -jar JavaBot.jar
cd ../../../reference-bot-publish
timeout /t 3
start "" dotnet ReferenceBot.dll
timeout /t 3
start "" dotnet ReferenceBot.dll
cd ../

pause