@echo off
:: Game Runner
::cd ./starter-bots/JavaBotH1
timeout /t 1
::start mvn clean package
::timeout /t 5
::cd ../..
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
start "Num1" java -jar JavaBot.jar
:: timeout /t 3
:: start "Num2" java -jar JavaBot.jar
:: cd ../../../reference-bot-publish
:: timeout /t 3
:: start "reference" dotnet ReferenceBot.dll
:: timeout /t 3
:: start "reference" dotnet ReferenceBot.dll


:: timeout /t 3
:: start "bangkit" java -jar Bangkit.jar
:: timeout /t 3
:: start "dillon" java -jar dilon.jar
timeout /t 3
start "leon" java -jar Leon.jar
timeout /t 3
start "nawwar" java -jar Nawwar.jar
timeout /t 3
start "hanan" java -jar hanan.jar
timeout /t 3
start "rava" java -jar RavaBot.jar
:: timeout /t 3
:: start "jovan" java -jar Blackmamba.jar
:: timeout /t 3
:: start "jovan" java -jar Blackmamba.jar
:: timeout /t 3
:: start "jovan" java -jar Blackmamba.jar
cd ../
:: timeout /t 3
:: start "" java -jar JavaBot.jar
:: cd ../../JavaBot/target
:: timeout /t 3
:: start "" java -jar JavaBot.jar
:: cd ../../JavaBotN2/target
:: timeout /t 3
:: start "" java -jar JavaBot.jar
:: cd ../

pause