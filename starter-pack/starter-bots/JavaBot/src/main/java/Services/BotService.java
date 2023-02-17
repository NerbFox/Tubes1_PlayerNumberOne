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

    private Boolean isNextTargetValid(GameObject object, List<GameObject> gasCloudList, List<GameObject> playerList) {
        var distError = 5;
        // Check targets (food and bots) near gas clouds
        if (!gasCloudList.isEmpty()) {
            for (GameObject gasCloud : gasCloudList) {
                if (getDistanceBetween(object, gasCloud) + distError <= gasCloud.size) {
                    System.out.println("Target not valid - Gas Clouds Ahead");
                    return false;
                }
            }
        }

        // Check food near enemy bot
        var distNear = 5;
        var degNear = 5;
        if (object.gameObjectType == ObjectTypes.FOOD) {
            if (!playerList.isEmpty()) {
                var dist = getDistanceBetween(object, playerList.get(1)) - bot.size - playerList.get(0).getSize();
                System.out.println(String.format("Dist to dangerous objects: %f", dist));
                if ((dist <= distNear)
                        && (getHeadingBetween(object) <= (getHeadingBetween(playerList.get(1)) + degNear))
                        && (getHeadingBetween(object) >= getHeadingBetween(playerList.get(1)) - degNear)) {
                    System.out.println("Food not valid - Object heading not good");
                    return false;
                }

            }
        }

        return true;

    }

    // Method for checking if an object is near the border map
    private boolean inBorderValid(GameObject object) {
        boolean inBorder = false;
        var rad = gameState.getWorld().getRadius();
        double distToBorderMin = rad * 0.05;

        if (!gameState.getGameObjects().isEmpty()) {
            if ((rad - (getDistancePosition(object, gameState.getWorld().getCenterPoint().x,
                    gameState.getWorld().getCenterPoint().y) + bot.getSize())) <= distToBorderMin) {
                inBorder = true;
                System.out.println("inBorder");
            } else {
                inBorder = false;
            }
        }
        return inBorder;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        // Intial player action
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        // Object lists sorted by a specific criteria
        var playerList = gameState.getPlayerGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var mostOftenTarget = gameState.getGameObjects().stream()
                .filter(item -> ((item.getGameObjectType() == ObjectTypes.FOOD)
                        || (item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                        || (item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)))
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var gasCloudList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var supernovaBombList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var teleporterList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());

        if (!gameState.getGameObjects().isEmpty()) {

            // Take action if target is still available
            if (!mostOftenTarget.isEmpty() && !playerList.isEmpty()) {
                // Index lists
                int collectibleIndex = 0;
                int nearestPlayerIndex = 1;

                // Possible targets
                var foodTarget = mostOftenTarget.get(collectibleIndex);
                var nearestPlayer = playerList.get(1);

                // Check food valid
                while (!isNextTargetValid(foodTarget, gasCloudList, playerList)) {
                    System.out.println("Change food target, food isnt valid");
                    if (collectibleIndex < mostOftenTarget.size()) {
                        collectibleIndex++;
                    } else {
                        foodTarget = mostOftenTarget.get(0);
                        collectibleIndex = 0;
                        break;
                    }
                }

                // Check nearest player is a valid target or not
                while (!isNextTargetValid(nearestPlayer, gasCloudList, playerList)) {
                    System.out.println("Change enemy target, enemy isnt valid");
                    if (nearestPlayerIndex < playerList.size()) {
                        nearestPlayerIndex++;
                    } else {
                        nearestPlayer = playerList.get(1);
                        nearestPlayerIndex = 1;
                        break;
                    }
                }
                nearestPlayer = playerList.get(nearestPlayerIndex);

                // Thresholds
                int safeDistancePlayer = 200 + nearestPlayer.getSize();
                int teleporterAttackThresholdSize = nearestPlayer.getSize() + 30;

                int torpedoThresholdSize = 25;
                double torpedoAttackThresholdDistance = 300 + nearestPlayer.getSize();

                double supernovaDetonateDistance = nearestPlayer.getSize() + 20;
                double teleporterDistanceThreshold = bot.size * 0.7;

                int passiveThresholdSize = nearestPlayer.getSize() + 10;

                var distNearestPlayer = getDistanceBetween(bot, nearestPlayer);
                var distNearestPlayerFixed = distNearestPlayer - nearestPlayer.size - bot.size;

                // Terminal log
                System.out.println(String.format("distNearestPlahyer = %f", distNearestPlayer));
                System.out.println(String.format("distNearestPlayerFixed = %f", distNearestPlayerFixed));
                System.out.println(String.format("safeDistancePlayer = %d", safeDistancePlayer));
                System.out.println(String.format("BOT SIZE = %d", bot.size));
                System.out.println(String.format("Telecount = %d", bot.teleCount));

                // Decision making
                if (bot.getSupernovaAvailable() > 0) { // Firing supernova if has one

                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    playerAction.action = PlayerActions.FIRESUPERNOVA;
                    System.out.println("Firing SUPERNOVA");

                } else if (inBorderValid(bot)) { // Check if bot near border
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = getHeadingPosition(bot, 0, 0);
                    System.out.println("Near border, go to center");
                } else if (bot.size < passiveThresholdSize) { // Passive mode

                    if (distNearestPlayerFixed <= safeDistancePlayer) { // Go to counter mode if enemy close to player
                                                                        // distance

                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FORWARD;

                        // Fire torpedoes if has more than 2 torpedoes and player size larger than
                        // threshold
                        if (bot.torpedoCount >= 2 && bot.size > torpedoThresholdSize) {
                            playerAction.heading = getHeadingBetween(nearestPlayer);
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            System.out.println("Firing torpedoes for safety");
                        } else { // Else flee to the same direction of enemy heading
                            playerAction.heading = (getHeadingBetween(nearestPlayer) - 180) % 360;
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("Kaboor");
                        }

                    } else { // Eat food if in a safe distance
                        playerAction.heading = getHeadingBetween(foodTarget);
                        playerAction.action = PlayerActions.FORWARD;
                        System.out.println("Eat food");
                    }

                } else { // Active mode

                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    System.out.println(distNearestPlayerFixed);
                    // Fire teleporters if have teleporter and player size larger than threshold
                    if (bot.teleCount > 0 && bot.size >= teleporterAttackThresholdSize) {
                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        System.out.println("Firing teleporter to enemy");

                    } else if (bot.torpedoCount >= 2 && distNearestPlayerFixed <= torpedoAttackThresholdDistance) {
                        // Fire torpedoes if player dont have teleporters and distance to enemy smaller
                        // then threshold
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        System.out.println("Close Enough - Firing torpedoes to enemy");
                    } else { // If dont have teleporters or torpedoes or threshold not fulfilled

                        // If close enough, chase enemy
                        if (distNearestPlayerFixed <= torpedoAttackThresholdDistance) {
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("Chasing enemy");
                        } else { // If enemy too far, search for food
                            playerAction.action = PlayerActions.FORWARD;
                            playerAction.heading = getHeadingBetween(foodTarget);
                            System.out.println("Enemy too far, going to eat food");
                        }
                    }
                }

                // If there are teleporters in map,
                if (!teleporterList.isEmpty()) {

                    // Get list of teleporter sorted by distance from target to teleporter
                    int idx = nearestPlayerIndex;
                    var teleListByTargetDist = gameState.getGameObjects().stream()
                            .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                            .sorted(Comparator.comparing(item -> getDistanceBetween(playerList.get(idx), item)))
                            .collect(Collectors.toList());
                    // Check for tele list to avoid bot crashing because of out of bounds error
                    if (!teleListByTargetDist.isEmpty()) {
                        System.out.println(String.format("teleporter distance = %f",
                                getDistanceBetween(nearestPlayer, teleListByTargetDist.get(0))
                                        + nearestPlayer.size));
                        // Fire teleporter if distance from target to teleporter fulfilled the threshold
                        if (getDistanceBetween(nearestPlayer, teleListByTargetDist.get(0))
                                - nearestPlayer.size < teleporterDistanceThreshold
                                && bot.size > nearestPlayer.size + 5) {

                            playerAction.action = PlayerActions.TELEPORT;
                            System.out.println("TELEPORTING");
                        }
                    }
                }

                // If supernova bomb in map,
                if (!supernovaBombList.isEmpty()) {
                    // Detonate supernova if distance is close enough with nearest player
                    if (getDistanceBetween(nearestPlayer, supernovaBombList.get(0)) < supernovaDetonateDistance) {
                        playerAction.action = PlayerActions.DETONATESUPERNOVA;
                        System.out.println("DETONATE SUPERNOVA");
                    }
                }
            } else { // If no target available, go to center
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingPosition(bot, 0, 0);
                System.out.println("No target available, go to center");
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

    // Method for calculating distance from an object to a certain position
    private double getDistancePosition(GameObject object1, int x, int y) {
        var triangleX = Math.abs(object1.getPosition().x - x);
        var triangleY = Math.abs(object1.getPosition().y - y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingPosition(GameObject object, int x, int y) {
        var direction = toDegrees(Math.atan2(y - object.getPosition().y, x - object.getPosition().x));
        return (direction + 360) % 360;
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
