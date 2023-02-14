package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public Boolean isFoodValid(GameObject food, List<GameObject> gasCloudList, List<GameObject> asteroidField) {
        ListIterator<GameObject> itGasCloud = gasCloudList.listIterator();
        ListIterator<GameObject> itAsteroidField = asteroidField.listIterator();
        while (itGasCloud.hasNext()) {
            if (getDistanceBetween(food, itGasCloud.next()) <= itGasCloud.next().size) {
                System.out.println("Food not valid - Gas Clouds Ahead");
                return false;
            }
        }

        while (itAsteroidField.hasNext()) {
            if (getDistanceBetween(food, itAsteroidField.next()) <= itAsteroidField.next().size) {
                System.out.println("Food not valid - Asteroid Fields Ahead");
                return false;
            }
        }

        System.out.println("Food valid");
        return true;

    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        var torpedoList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var playerList = gameState.getPlayerGameObjects().stream()
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var foodList = gameState.getGameObjects().stream()
                .filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD
                        || (item.getGameObjectType() == ObjectTypes.SUPERFOOD)))
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var wormholeList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.WORMHOLE)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var gasCloudList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var asteroidFieldList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var superfoodList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var supernovaList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var supernovaBombList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());

        // Harus ditambah kasus food abis

        if (!gameState.getGameObjects().isEmpty()) {
            Integer foodIndex = 0;
            var foodTarget = foodList.get(foodIndex);
            while (!isFoodValid(foodTarget, gasCloudList, asteroidFieldList)) {
                System.out.println("test");
                foodIndex++;
                foodTarget = foodList.get(foodIndex);
            }

            if (bot.size < playerList.get(1).size) {
                // var distance = getDistanceBetween(bot, playerList.get(1));
                // if (distance - playerList.get(1).size / 2 <= 20) {
                // playerAction.heading = (getHeadingBetween(playerList.get(1)) - 180) % 360;
                // playerAction.action = PlayerActions.FIRETORPEDOES;
                // } else {
                // }
                playerAction.heading = getHeadingBetween(foodTarget);

            } else {
                playerAction.heading = getHeadingBetween(playerList.get(1));
                if (bot.torpedoCount <= 3) {
                    playerAction.action = PlayerActions.FORWARD;
                } else {
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                }
            }

        }

        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream()
                .filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

}
