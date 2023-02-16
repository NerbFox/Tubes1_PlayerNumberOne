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

        // System.out.println("Food valid");
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

            if (!mostOftenTarget.isEmpty()) {
                // Index lists
                Integer collectibleIndex = 0;
                Integer smallestPlayerIndex = 0;
                // Possible targets
                var foodTarget = mostOftenTarget.get(collectibleIndex);
                var nearestPlayer = playerList.get(1);
                var nearestPlayerDistance = getDistanceBetween(bot, nearestPlayer);
                var smallestPlayer = playerListBySize.get(smallestPlayerIndex);
                var smallestPlayerDistance = getDistanceBetween(bot, smallestPlayer);
                var biggestPlayer = playerListBySize.get(playerListBySize.size() - 1);

                // Thresholds
                // boolean isAfterBurnerOff = (bot.effects == 0 || bot.effects == 2 ||
                // bot.effects == 4
                // || bot.effects == 6);

                Integer safeDistancePlayer = 20 + nearestPlayer.getSize();

                Integer teleporterFleeThresholdSize = 40;
                Integer teleporterAttackThresholdSize = smallestPlayer.getSize() + 35;

                Integer shieldAttackThresholdSize = 50;

                Integer torpedoThresholdSize = 25;

                Integer supernovaDetonateDistance = nearestPlayer.getSize() + 20;

                Integer teleporterDistanceThreshold = bot.size + smallestPlayer.getSize() - 15;

                // Integer afterburnerAttackSizeThreshold = 50;
                // Integer afterburnerDefenseSizeThreshold = 30;

                Integer attackThresholdSize = 50;

                var distance = getDistanceBetween(bot, nearestPlayer);
                var distanceFixed = distance - nearestPlayer.size - bot.size;

                System.out.println(String.format("Telecount = %d", bot.teleCount));
                // Check food valid
                while (!isNextTargetValid(foodTarget, gasCloudList, asteroidFieldList, playerList)) {
                    System.out.println("Change food target, food isnt valid");
                    collectibleIndex++;
                    foodTarget = mostOftenTarget.get(collectibleIndex);
                }

                // Get smallest player
                while (smallestPlayer.getId() == bot.id) {
                    smallestPlayerIndex++;
                    smallestPlayer = playerListBySize.get(smallestPlayerIndex);
                    smallestPlayerDistance = getDistanceBetween(bot, smallestPlayer);
                }

                if (bot.getSupernovaAvailable() > 0) { // Firing supernova if has one

                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    playerAction.action = PlayerActions.FIRESUPERNOVA;
                    System.out.println("Firing SUPERNOVA");

                } else if (bot.size < nearestPlayer.size + 25) { // Passive mode

                    if (distanceFixed <= safeDistancePlayer) { // Kondisi ketika player distance lebih kecil dari
                                                               // safe distance

                        playerAction.heading = getHeadingBetween(nearestPlayer) - 180;
                        // if (bot.teleCount > 0 && bot.size >= teleporterFleeThresholdSize) { // Kalau
                        // punya teleporter
                        // // tepat 1, pake teleporter
                        // // buat kabur
                        // playerAction.heading = getHeadingBetween(nearestPlayer) - 180;
                        // playerAction.action = PlayerActions.FIRETELEPORT;
                        // System.out.println("Firing teleport to flee");
                        if (bot.torpedoCount >= 2 && bot.size > torpedoThresholdSize) { // Kalau gaada
                                                                                        // afterburner dan
                                                                                        // teleporternya ga tepat
                                                                                        // 1, tembak torpedo
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
                        System.out.println("Eat food");
                    }

                } else { // Active mode

                    playerAction.heading = getHeadingBetween(nearestPlayer);
                    if (bot.teleCount > 0 && teleporterAttackThresholdSize <= bot.size && !teleporterList.isEmpty()) { // Kalau
                                                                                                                       // punya
                                                                                                                       // teleporter
                                                                                                                       // dan
                        // threshold size mencukupi,
                        // tembak teleporter
                        playerAction.heading = getHeadingBetween(smallestPlayer);
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        System.out.println("Firing teleporter to enemy");
                    } else if (bot.torpedoCount >= 3 && 50 >= distanceFixed) {
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        System.out.println("Close Enough - Firing torpedoes to enemy");
                    } else {
                        if (nearestPlayerDistance <= nearestPlayer.size * 3) {
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
                    if (getDistanceBetween(smallestPlayer, teleporterList.get(0)) < teleporterDistanceThreshold) {
                        // Teleport ke lokasi teleporter ketika teleporter sudah
                        // dekat dengan target
                        playerAction.action = PlayerActions.TELEPORT;
                        System.out.println("TELEPORTING");
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
