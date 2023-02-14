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
            if (bot.size < 30) {
                System.out.println("Too small, searching for food");
                playerAction.heading = getHeadingBetween(foodList.get(0));
                playerAction.action = PlayerActions.FORWARD;
            } else {
                playerAction.heading = getHeadingBetween(playerList.get(1));
                if (playerList.get(1).size > bot.size) {
                    System.out.println("Opponent too big, searching for food");
                    playerAction.heading = -getHeadingBetween(playerList.get(1));
                    if (getDistanceBetween(bot, playerList.get(1)) > playerList.get(1).size * 5.5) {
                        playerAction.heading = getHeadingBetween(playerList.get(1));
                    }
                    playerAction.action = PlayerActions.FORWARD;
                } else {
                    if (getDistanceBetween(bot, playerList.get(1)) > playerList.get(1).size * 4) {
                        if (getDistanceBetween(bot, playerList.get(1)) > playerList.get(1).size * 6
                                && (bot.effects == 0 || bot.effects == 2 || bot.effects == 4 || bot.effects == 6)
                                && bot.size > 48) {
                            System.out.println("Really far away, using afterburner");
                            playerAction.action = PlayerActions.STARTAFTERBURNER;
                        } else if (getDistanceBetween(bot, playerList.get(1)) <= playerList.get(1).size * 6
                                && (bot.effects == 1 || bot.effects == 3 || bot.effects == 5 || bot.effects == 7)
                                && bot.size <= 36) {
                            System.out.println("Getting closer, turning off afterburner");
                            playerAction.action = PlayerActions.STOPAFTERBURNER;
                        } else {
                            System.out.println("Moving closer");
                            playerAction.action = PlayerActions.FORWARD;
                        }
                        // System.out.println("Too Far, getting closer");
                        // playerAction.action = PlayerActions.FORWARD;
                    } else {
                        System.out.println("Close enough, firing!");
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                    }
                }
                if (playerAction.heading != getHeadingBetween(playerList.get(1))
                        && (bot.effects == 1 || bot.effects == 3 || bot.effects == 5 || bot.effects == 7)) {
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
