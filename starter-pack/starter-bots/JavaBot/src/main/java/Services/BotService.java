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

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.STOP;

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
        var supernovaList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());
        var supernovaBombList = gameState.getGameObjects().stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).collect(Collectors.toList());

        if (!gameState.getGameObjects().isEmpty()) {
            var nearestPlayer = playerList.get(1);
            var nearestFood = foodList.get(0);
            boolean isAfterBurnerOn = (bot.effects == 0 || bot.effects == 2 || bot.effects == 4 || bot.effects == 6);
            var sizeDiffWithPlayer = nearestPlayer.size - bot.size;
            if (bot.size < 30) { /* Finding food if too small */
                System.out.println("Too small, searching for food");
                playerAction.heading = getHeadingBetween(nearestFood);
                playerAction.action = PlayerActions.FORWARD;
            } else { /* Size big enough, start calculating attack */
                playerAction.heading = getHeadingBetween(nearestPlayer);
                if (sizeDiffWithPlayer > bot.size) { /* Enemy too big */
                    System.out.println("Opponent too big!");
                    if (getDistanceBetween(bot, nearestPlayer) > nearestPlayer.size + bot.size + 300) { /*
                                                                                                         * Shooting from
                                                                                                         * safe distance
                                                                                                         */
                        System.out.println("Far enough, shooting!");
                        playerAction.heading = getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                    } else { /* Getting away */
                        System.out.println("Heading away..");
                        playerAction.heading = -getHeadingBetween(nearestPlayer);
                        playerAction.action = PlayerActions.FORWARD;
                    }
                } else { /* Valid enemy to attack */
                    if (getDistanceBetween(bot, nearestPlayer) > nearestPlayer.size * 3) { /* Need to get closer */
                        if (getDistanceBetween(bot, nearestPlayer) > nearestPlayer.size * 5
                                && !isAfterBurnerOn
                                && bot.size > 48) { /* Too far away */
                            System.out.println("Really far away, using afterburner");
                            playerAction.action = PlayerActions.STARTAFTERBURNER;
                        } else if (getDistanceBetween(bot, nearestPlayer) <= nearestPlayer.size * 5
                                && getDistanceBetween(bot, nearestPlayer) > nearestPlayer.size * 4
                                && isAfterBurnerOn
                                && bot.size <= 36) { /* Getting closer */
                            System.out.println("Getting closer, turning off afterburner");
                            playerAction.action = PlayerActions.STOPAFTERBURNER;
                        } else { /* Close enough */
                            System.out.println("Moving closer");
                            playerAction.action = PlayerActions.FORWARD;
                        }
                        // System.out.println("Too Far, getting closer");
                        // playerAction.action = PlayerActions.FORWARD;
                    } else { /* firing from close distance */
                        System.out.println("Close enough, firing!");
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                    }
                }
                if (playerAction.heading != getHeadingBetween(nearestPlayer)
                        && isAfterBurnerOn) { /* Make sure afterburner off while not going towards enemy */
                    System.out.println("Not going after enemy, stopping afterburner");
                    playerAction.action = PlayerActions.STOPAFTERBURNER;
                }
            }

            this.playerAction = playerAction;
        }
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
