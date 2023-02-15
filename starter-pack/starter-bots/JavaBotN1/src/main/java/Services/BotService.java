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

    private Boolean isNextTargetValid(GameObject object, List<GameObject> gasCloudList,
            List<GameObject> asteroidFieldList, List<GameObject> playerList) {
        var distError = 5;
        if (!gasCloudList.isEmpty()) {
            for (GameObject gasCloud : gasCloudList) {
                if (getDistanceBetween(object, gasCloud) + distError <= gasCloud.size) {
                    System.out.println("Food not valid - Gas Clouds Ahead");
                    return false;
                }
            }
        }

        if (!asteroidFieldList.isEmpty()) {
            for (GameObject asteroidField : asteroidFieldList) {
                if (getDistanceBetween(object, asteroidField) + distError <= asteroidField.size) {
                    System.out.println("Food not valid - Asteroid Fields Ahead");
                    return false;
                }
            }
        }
        // heading if ini food baru lakuin ini
        var distNear = 10;
        var degNear = 5;
        if (!playerList.isEmpty()) {
            var dist = getDistanceBetween(object, playerList.get(1)) - bot.size - playerList.get(0).getSize();
            if ((dist <= distNear) && (getHeadingBetween(object) <= (getHeadingBetween(playerList.get(1)) + distNear))
                    && (getHeadingBetween(object) >= getHeadingBetween(playerList.get(1)) - distNear)) {
                System.out.println("Food not valid - Object heading not good");
                return false;
            }
            // for(GameObject player : playerList ){

        }
        // inBorder
        if (inBorderValid(object)) {
            System.out.println("Food not valid - Food mau keluar");
            return false;
        }

        System.out.println("Food valid");
        return true;

    }

    private boolean inBorderValid(GameObject object) {
        boolean inBorder = false;
        double distToBorderMin = 15.0;
        var rad = gameState.getWorld().getRadius();

        if (!gameState.getGameObjects().isEmpty()) {
            if ((rad - (getDistancePosition(object, 0, 0) + object.getSize())) <= distToBorderMin) {
                inBorder = true;
                System.out.println("inBorder");
            } else {
                inBorder = false;
                System.out.println("outBorder");
            }
        }
        return inBorder;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        var torpedoList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var playerList = gameState.getPlayerGameObjects().stream()
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var foodList = gameState.getGameObjects().stream()
                .filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD
                        || (item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                        || (item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)))
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var wormholeList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.WORMHOLE)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var gasCloudList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
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
        var asteroidFieldList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
                

        // algoritma yang diinginkan adalah: greedy, kalo ada food yang searah sama bot
        // lawan, maka bot akan menghindar !! lalu jika tidak ada food yang searah, maka
        // bot akan mengambil food yang terdekat
        // size itu radius apa diameter?
        // di valid food tambahin inborder
        // n heding klo ada food yang searah sama bot lawan

        int botSize = 30;
        boolean inBorder = false;
        playerAction.heading = new Random().nextInt(360);
        // if (bot.size < botSize && !superfoodList.isEmpty() && !inBorder){
        if (!gameState.getGameObjects().isEmpty()) {
            Integer foodIndex = 0;
            var foodTarget = foodList.get(foodIndex);
            while (!isNextTargetValid(foodTarget, gasCloudList, asteroidFieldList, playerList)) {
                System.out.println("test");
                foodIndex++;
                foodTarget = foodList.get(foodIndex);
            }
            // if(supernovaList.isEmpty())
            if (bot.getSupernovaAvailable() > 0) {
                playerAction.heading = getHeadingBetween(playerList.get(1));
                playerAction.action = PlayerActions.FIRESUPERNOVA;
            } else {
                if (bot.size < botSize) {
                    playerAction.action = PlayerActions.FORWARD;
                    if (!gameState.getGameObjects().isEmpty()) { // if there are any game objects
                        playerAction.heading = getHeadingBetween(foodTarget);

                        // if (!inBorder) {
                        // playerAction.heading = getHeadingBetween(foodList.get(0));
                        // } else {
                        // playerAction.heading = getHeadingBetween(foodList.get(1));
                        // }
                    }
                } else {
                    if (bot.size > playerList.get(1).size) {
                        playerAction.heading = getHeadingBetween(playerList.get(1));
                        playerAction.action = PlayerActions.FORWARD;
                    } else {
                        playerAction.heading = getHeadingBetween(playerList.get(1));
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                    }
                }
            }
            // else{
            // playerAction.action = PlayerActions.FORWARD;
            // playerAction.heading = getHeadingBetween(supernovaList.get(0));
            // }
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

    private double getDistancePosition(GameObject object1, int x, int y) {
        var triangleX = Math.abs(object1.getPosition().x - x);
        var triangleY = Math.abs(object1.getPosition().y - y);
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
