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
        // Gas cloud
        if (!gasCloudList.isEmpty()) {
            for (GameObject gasCloud : gasCloudList) {
                if (getDistanceBetween(object, gasCloud) + distError <= gasCloud.size) {
                    System.out.println("Target not valid - Gas Clouds Ahead");
                    return false;
                }
            }
        }

        if (!asteroidFieldList.isEmpty()) {
            for (GameObject asteroidField : asteroidFieldList) {
                if (getDistanceBetween(object, asteroidField) + distError <= asteroidField.size) {
                    System.out.println("Target not valid - Asteroid Fields Ahead");
                    return false;
                }
            }
        }
        // heading if ini food baru lakuin ini
        var distNear = 10;
        var degNear = 5;
        if (object.gameObjectType == ObjectTypes.FOOD) {
            if (!playerList.isEmpty()) {
                var dist = getDistanceBetween(object, playerList.get(1)) - bot.size - playerList.get(0).getSize();
                if ((dist <= distNear)
                        && (getHeadingBetween(object) <= (getHeadingBetween(playerList.get(1)) + degNear))
                        && (getHeadingBetween(object) >= getHeadingBetween(playerList.get(1)) - degNear)) {
                    System.out.println("Food not valid - Object heading not good");
                    return false;
                }
                // for(GameObject player : playerList ){

            }
        }

        // inBorder
        if (inBorderValid(object)) {
            System.out.println("Target not valid - target inborder");
            return false;
        }

        // System.out.println("Food valid");
        return true;

    }

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
                // System.out.println("outBorder");
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
                .filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var playerListBySize = gameState.getPlayerGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator.comparing(item -> item.size)).collect(Collectors.toList());
        var mostOftenTarget = gameState.getGameObjects().stream()
                .filter(item -> ((item.getGameObjectType() == ObjectTypes.FOOD)
                        || (item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                        || (item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)))
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
        var supernovaBombList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var teleporterList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());

        if (!gameState.getGameObjects().isEmpty()) {

            if (!mostOftenTarget.isEmpty() && !playerList.isEmpty() && !playerListBySize.isEmpty()) {
                // Index lists
                int collectibleIndex = 0;
                int smallestPlayerIndex = 0;
                int nearestPlayerIndex = 1;
                // Possible targets
                var foodTarget = mostOftenTarget.get(collectibleIndex);
                var nearestPlayer = playerList.get(1);
                var smallestPlayer = playerListBySize.get(smallestPlayerIndex);
                var nearestPlayerDistance = getDistanceBetween(bot, nearestPlayer);
                var smallestPlayerDistance = getDistanceBetween(bot, smallestPlayer);
                var biggestPlayer = playerListBySize.get(playerListBySize.size() - 1);

                // Check food valid
                while (!isNextTargetValid(foodTarget, gasCloudList, asteroidFieldList, playerList)) {
                    System.out.println("Change food target, food isnt valid");
                    if (collectibleIndex < mostOftenTarget.size()) {
                        collectibleIndex++;
                    } else {
                        foodTarget = mostOftenTarget.get(0);
                        collectibleIndex = 0;
                        break;
                    }
                }

                // Get nearest player
                while (!isNextTargetValid(nearestPlayer, gasCloudList, asteroidFieldList, playerList)) {
                    System.out.println("Change enemy target, enemy isnt valid");
                    if (nearestPlayerIndex < playerList.size()) {
                        nearestPlayerIndex++;
                    } else {
                        nearestPlayer = playerList.get(0);
                        nearestPlayerIndex = 0;
                        break;
                    }
                }

                // Get smallest player
                while (!isNextTargetValid(smallestPlayer, gasCloudList, asteroidFieldList, playerList)) {
                    if (smallestPlayerIndex < playerListBySize.size() || smallestPlayer.getId() == bot.id) {
                        System.out.println("Change smallest enemy target, smallest enemy isnt valid");
                        smallestPlayerIndex++;
                    } else {
                        smallestPlayer = playerListBySize.get(0);
                        smallestPlayerIndex = 0;
                        break;
                    }
                }

                nearestPlayer = playerList.get(nearestPlayerIndex);
                smallestPlayer = playerListBySize.get(smallestPlayerIndex);

                // Thresholds
                int safeDistancePlayer = 150 + nearestPlayer.getSize();
                int teleporterFleeThresholdSize = 40;
                int teleporterAttackThresholdSize = smallestPlayer.getSize() + 30;
                int shieldAttackThresholdSize = 50;

                int torpedoThresholdSize = 25;
                double torpedoAttackThresholdDistance = 250;

                double supernovaDetonateDistance = nearestPlayer.getSize() + 20;
                double teleporterDistanceThreshold = bot.size * 0.9;

                // int afterburnerAttackSizeThreshold = 50;
                // int afterburnerDefenseSizeThreshold = 30;

                int attackThresholdSize = 50;
                int passiveThresholdSize = nearestPlayer.getSize() + 10;

                var distNearestPlayer = getDistanceBetween(bot, nearestPlayer);
                System.out.println(String.format("distNearestPlahyer = %f", distNearestPlayer));
                var distNearestPlayerFixed = distNearestPlayer - nearestPlayer.size - bot.size;
                System.out.println(String.format("distNearestPlayerFixed = %f", distNearestPlayerFixed));
                System.out.println(String.format("safeDistancePlayer = %d", safeDistancePlayer));

                // System.out.println(String.format("Telecount = %d", bot.teleCount));

                if (bot.getSupernovaAvailable() > 0) { // Firing supernova if has one

                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    playerAction.action = PlayerActions.FIRESUPERNOVA;
                    System.out.println("Firing SUPERNOVA");

                } else if (bot.size < passiveThresholdSize) { // Passive mode

                    if (distNearestPlayerFixed <= safeDistancePlayer) { // Kondisi ketika player distNearestPlayer lebih
                                                                        // kecil dari
                        // safe distNearestPlayer

                        playerAction.heading = getHeadingBetween(nearestPlayer) - 180;
                        playerAction.action = PlayerActions.FORWARD;
                        // if (bot.teleCount > 0 && bot.size >= teleporterFleeThresholdSize) { // Kalau
                        // punya teleporter
                        // // tepat 1, pake teleporter
                        // // buat kabur
                        // playerAction.heading = getHeadingBetween(nearestPlayer) - 180;
                        // playerAction.action = PlayerActions.FIRETELEPORT;
                        // System.out.println("Firing teleport to flee");
                        if (bot.torpedoCount >= 2 && bot.size > torpedoThresholdSize) {
                            playerAction.heading = getHeadingBetween(nearestPlayer);
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            System.out.println("Firing torpedoes for safety");
                        } else { // Else kabur 180 derajat
                            playerAction.heading = getHeadingBetween(nearestPlayer) - 180;
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("Kaboor");
                        }

                    } else { // Eat food
                        playerAction.heading = getHeadingBetween(foodTarget);
                        playerAction.action = PlayerActions.FORWARD;
                        System.out.println("Eat food");
                    }

                } else { // Active mode

                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    System.out.println(distNearestPlayerFixed);

                    if (bot.teleCount > 0 && teleporterAttackThresholdSize <= bot.size) {
                        // threshold sisze mencukupi,
                        // tembak teleporter
                        playerAction.heading = getHeadingBetween(smallestPlayer);
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        System.out.println("Firing teleporter to enemy");
                    } else if (bot.torpedoCount >= 2 && distNearestPlayerFixed <= torpedoAttackThresholdDistance) {
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        System.out.println("Close Enough - Firing torpedoes to enemy");
                    } else {
                        if (distNearestPlayerFixed <= torpedoAttackThresholdDistance) {
                            playerAction.action = PlayerActions.FORWARD;
                            System.out.println("Chasing enemy");
                        } else {
                            playerAction.action = PlayerActions.FORWARD;
                            playerAction.heading = getHeadingBetween(foodTarget);
                            System.out.println("Enemy too far, going to eat food");
                        }
                    }
                }
                if (!teleporterList.isEmpty()) { // Jika dalam map ada teleporter yang sudah ditembakkan
                    int idx = smallestPlayerIndex;
                    var teleListByTargetDist = gameState.getGameObjects().stream()
                            .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                            .sorted(Comparator.comparing(item -> getDistanceBetween(playerListBySize.get(idx), item)))
                            .collect(Collectors.toList());
                    if (!teleListByTargetDist.isEmpty()) {
                        System.out.println(String.format("teleporter distance = %f",
                                getDistanceBetween(smallestPlayer, teleListByTargetDist.get(0))
                                        + smallestPlayer.size));
                        if (getDistanceBetween(smallestPlayer, teleListByTargetDist.get(0))
                                + smallestPlayer.size < teleporterDistanceThreshold
                                && bot.size > smallestPlayer.size + 5) {
                            // Teleport ke lokasi teleporter ketika teleporter sudah
                            // dekat dengan target
                            playerAction.action = PlayerActions.TELEPORT;
                            System.out.println("TELEPORTING");
                        }
                    }
                }

                if (!supernovaBombList.isEmpty()) {
                    if (getDistanceBetween(nearestPlayer, supernovaBombList.get(0)) < supernovaDetonateDistance) {
                        playerAction.action = PlayerActions.DETONATESUPERNOVA;
                        System.out.println("DETONATE SUPERNOVA");
                    }
                }
            } else {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingPosition(bot, 0, 0);
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
