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
            if ((dist <= distNear) && (getHeadingBetween(object) <= (getHeadingBetween(playerList.get(1)) + degNear))
                    && (getHeadingBetween(object) >= getHeadingBetween(playerList.get(1)) - degNear)) {
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
        var teleporterList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());

        // Harus ditambah kasus food abis
        // System.out.println("food");
        // System.out.println(foodList);
        // System.out.println("gascloud");
        // System.out.println(gasCloudList);
        // System.out.println("asteroid");
        // System.out.println(asteroidFieldList);

        Integer safeDistancePlayer = 30;
        Integer teleporterFleeThresholdSize = 40;
        Integer teleporterAttackThresholdSize = 60;
        Integer shieldAttackThresholdSize = 60;

        if (!gameState.getGameObjects().isEmpty()) {

            if (!foodList.isEmpty()) {
                Integer foodIndex = 0;
                var foodTarget = foodList.get(foodIndex);
                while (!isNextTargetValid(foodTarget, gasCloudList, asteroidFieldList, playerList)) {
                    System.out.println("test");
                    foodIndex++;
                    foodTarget = foodList.get(foodIndex);
                }

                if (bot.size < playerList.get(1).size + 20) {
                    var distance = getDistanceBetween(bot, playerList.get(1));
                    if (distance - playerList.get(1).size - bot.size <= safeDistancePlayer) {

                        if (bot.teleCount > 0 && bot.size >= teleporterFleeThresholdSize) {
                            playerAction.heading = playerList.get(1).getCurrentHeading();
                            playerAction.action = PlayerActions.FIRETELEPORT;
                            System.out.println("Firing teleport to flee");
                        } else if (bot.torpedoCount >= 2) {
                            playerAction.heading = getHeadingBetween(playerList.get(1));
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            System.out.println("Firing torpedoes");
                        } else {
                            playerAction.heading = playerList.get(1).getCurrentHeading();
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("Kaboor");
                        }

                    } else {
                        playerAction.heading = getHeadingBetween(foodTarget);
                        System.out.println("eat food");
                    }

                } else {
                    playerAction.heading = getHeadingBetween(playerList.get(1));
                    if (bot.teleCount > 0 && bot.size >= teleporterAttackThresholdSize) {
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        System.out.println("Fire teleporter to enemy");
                    } else if (bot.torpedoCount >= 2) {
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        System.out.println("Firing torpedoes to enemy");
                    } else {
                        playerAction.action = PlayerActions.FORWARD;
                        System.out.println("Chasing enemy");
                    }
                }

                if (!teleporterList.isEmpty()) {
                    if (getDistanceBetween(bot, teleporterList.get(0)) < getDistanceBetween(bot, playerList.get(1))
                            - 25) {
                        playerAction.action = PlayerActions.TELEPORT;
                        System.out.println("TELEPORTING");
                    }
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
