package com.enriclop.logrosbot.twitchConnection;

import com.enriclop.logrosbot.achievementApi.AchievementGeneration;
import com.enriclop.logrosbot.enums.Pokeballs;
import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.security.Settings;
import com.enriclop.logrosbot.servicio.AchievementService;
import com.enriclop.logrosbot.servicio.ItemsService;
import com.enriclop.logrosbot.servicio.UserService;
import com.enriclop.logrosbot.twitchConnection.commands.*;
import com.enriclop.logrosbot.twitchConnection.events.Event;
import com.enriclop.logrosbot.twitchConnection.events.SpawnEvent;
import com.enriclop.logrosbot.twitchConnection.rewards.CatchReward;
import com.enriclop.logrosbot.twitchConnection.rewards.Reward;
import com.enriclop.logrosbot.twitchConnection.rewards.SuperCatchReward;
import com.enriclop.logrosbot.twitchConnection.settings.Prices;
import com.enriclop.logrosbot.twitchConnection.threads.Spawn;
import com.enriclop.logrosbot.twitchConnection.threads.Trade;
import com.enriclop.logrosbot.utilities.Utilities;
import com.enriclop.logrosbot.websockets.card.CardInfoService;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.helix.domain.Chatter;
import com.github.twitch4j.helix.domain.ChattersList;
import com.github.twitch4j.helix.domain.Moderator;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.github.twitch4j.util.PaginationUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Getter
@Setter
@Slf4j
public class TwitchConnection {

    @Autowired
    UserService userService;

    @Autowired
    AchievementService achievementService;

    @Autowired
    ItemsService itemsService;

    @Autowired
    private Settings settings;

    @Autowired
    Prices prices;

    @Autowired
    CardInfoService cardInfoClient;

    Achievement wildAchievement;

    TwitchClient twitchClient;

    EventManager eventManager;

    Trade currentTrade;

    Spawn spawn;

    List<Command> commands;

    List<Reward> rewards;

    List<Event> events;

    OAuth2Credential streamerCredential;

    com.github.twitch4j.helix.domain.User channel;

    SetWatchTime setWatchTime;

    private Map<String, List<String>> cooldowns = new HashMap<>();

    @Autowired
    private AchievementGeneration achievementGeneration;

    public TwitchConnection() {
        this.commands = new ArrayList<>();
        this.rewards = new ArrayList<>();
        this.events = new ArrayList<>();

        // Initialize commands
        commands.add(new HelpCommand());
        commands.add(new GiftCommand());
        commands.add(new CatchCommand());
        commands.add(new LeaderboardCommand());
        commands.add(new InventoryCommand());
        commands.add(new SpawnCommand());
        commands.add(new TradeCommand());
        commands.add(new WatchtimeCommand());
        commands.add(new RefreshUsernameCommand());
        commands.add(new PointsCommand());


        // Initialize rewards
        rewards.add(new CatchReward());
        rewards.add(new SuperCatchReward());

        // Initialize events
        events.add(new SpawnEvent());
    }


    @PostConstruct
    public void connect() {

        if (settings == null) {
            throw new IllegalStateException("Settings bean is not initialized");
        }

        if (twitchClient != null) {
            twitchClient.close();
        }

         twitchClient = TwitchClientBuilder.builder()
                 .withDefaultAuthToken(new OAuth2Credential(settings.getBotUsername(), settings.getoAuthTokenBot()))
                 .withEnableHelix(true)
                 .withEnableChat(true)
                 .withEnablePubSub(true)
                 .withChatAccount(new OAuth2Credential(settings.getBotUsername(), settings.getoAuthTokenBot()))
                 .build();

         twitchClient.getChat().joinChannel(settings.getChannelName());

         channel = getUserDetails(settings.getChannelName());

         streamerCredential = new OAuth2Credential("twitch", settings.getoAuthTokenChannel());

         twitchClient.getPubSub().listenForChannelPointsRedemptionEvents(null, channel.getId());

         //twitchClient.getPubSub().listenForSubscriptionEvents(streamerCredential, channel.getId());
         //twitchClient.getClientHelper().enableFollowEventListener(settings.getChannelName());

         commands();

         setWatchTime = new  SetWatchTime(this);
         setWatchTime.start();

         sendMessage("Bot Online");
    }

    public void sendMessage(String message) {
        twitchClient.getChat().sendMessage(settings.getChannelName(), message);
    }

    public void sendWhisper(String userID, String message) {

        String tokenBot = settings.getTokenBot();
        String botUsername = settings.getBotUsername();
        String botID = getUserDetails(botUsername).getId();

        twitchClient.getHelix().sendWhisper(tokenBot, botID, userID, message).execute();
    }

    public void commands() {
        eventManager = twitchClient.getEventManager();

        eventManager.onEvent(ChannelMessageEvent.class, event -> {
            if (!isLive() && !checkMod(event.getUser().getId())) return;

            if (!event.getMessage().startsWith("!")) return;

            start(event.getUser().getId());

            String command = event.getMessage().split(" ")[0];
            String finalCommand = command.toLowerCase();
            Command commandCalled = commands.stream().filter(c -> c.getCommand().equals(finalCommand)).findFirst().orElse(null);

            if (commandCalled == null) return;
            if (!commandCalled.isActive()) return;
            if (commandCalled.isModOnly() && !checkMod(event.getUser().getId())) return;
            if (commandCalled.getPrice() > 0 && !checkPoints(commandCalled, event.getUser().getId())) return;
            if (commandCalled.getCooldown() > 0 && checkCooldown(commandCalled.getCommand(), event.getUser().getId())) return;

            commandCalled.execute(this, event);
        });

        eventManager.onEvent(RewardRedeemedEvent.class, event -> {
            if (!isLive() && !checkMod(event.getRedemption().getUser().getId())) return;

            String reward = event.getRedemption().getReward().getTitle();

            String finalReward = reward.toLowerCase();
            Reward rewardCalled = rewards.stream().filter(r -> r.getReward().toLowerCase().equals(finalReward)).findFirst().orElse(null);

            if (rewardCalled == null) return;
            if (!rewardCalled.isActive()) return;
            if (rewardCalled.isModOnly() && !checkMod(event.getRedemption().getUser().getId())) return;
            if (rewardCalled.getCooldown() > 0 && checkCooldown(rewardCalled.getReward(), event.getRedemption().getUser().getId())) {
                returnRedemption(event.getRedemption());
                return;
            }


            rewardCalled.execute(this, event);
        });

        eventManager.onEvent(FollowEvent.class , event -> {
            followingReward(event.getUser().getId());
        });

        eventManager.onEvent(ChannelSubscribeEvent.class, event -> {
            subReward(event.getData().getUserId());
        });

    }

    private boolean checkPoints(Command commandCalled, String id) {
        User user = userService.getUserByTwitchId(id);
        if (user == null) {
            start(id);
            user = userService.getUserByTwitchId(id);
        }

        if (user.getScore() < commandCalled.getPrice()) {
            sendMessage("No tienes suficientes puntos para usar este comando!");
            return false;
        }

        if (commandCalled.getPrice() > 0) {
            user.setScore(user.getScore() - commandCalled.getPrice());
            userService.saveUser(user);
        }

        return true;
    }

    public boolean checkMod(String userId) {
        List<User> mods = settings.getModeratorUsers();

        return mods.stream().anyMatch(mod -> (mod.getTwitchId() + "").equals(userId));
    }

    @Scheduled(fixedRate = 36000000)
    public void getAllMods() {
        List<User> mods = new ArrayList<>();
        mods.add(userService.getUserByTwitchId(channel.getId()));
        for (String mod : settings.getModerators()) {
            User user = userService.getUserByUsername(mod);
            if (user != null) mods.add(user);
        }
        for (Moderator mod : twitchClient.getHelix().getModerators(settings.getTokenChannel(), getChannel().getId(), null, null, null).execute().getModerators()) {
            User user = userService.getUserByTwitchId(mod.getUserId());
            if (user != null) mods.add(user);
        }

        settings.setModeratorUsers(mods);
    }

    public boolean checkCooldown(String command, String id) {
        if (cooldowns.containsKey(command)) {
            List<String> users = cooldowns.get(command);
            if (users.contains(id)) {
                //sendMessage("Espera un momento antes de volver a usar este comando!");
                return true;
            } else {
                cooldowns.put(command, users);
            }
        } else {
            List<String> users = new ArrayList<>();
            cooldowns.put(command, users);
        }

        return false;
    }

    public void addCooldown(String command, String id, int minutesCD) {
        if (cooldowns.containsKey(command)) {
            List<String> users = cooldowns.get(command);
            users.add(id);
        } else {
            List<String> users = new ArrayList<>();
            users.add(id);
            cooldowns.put(command, users);
        }

        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cooldowns.get(command).remove(id);
            }
        }, (long) minutesCD * 60 * 1000);
    }

    public Collection<Chatter> getChatters() {

        return PaginationUtil.getPaginated(
                cursor -> {
                    try {
                        return twitchClient.getHelix().getChatters(settings.getTokenChannel(), channel.getId(), channel.getId(), 1000, cursor).execute();
                    } catch (Exception e) {
                        log.error("Error", e);
                        return null;
                    }
                },
                ChattersList::getChatters,
                call -> call.getPagination() != null ? call.getPagination().getCursor() : null
        );
    }

    public List<User> getChattersUsers() {
        Collection<Chatter> chatters = getChatters();
        List<User> users = new ArrayList<>();
        for (Chatter chatter : chatters) {
            User user = userService.getUserByTwitchId(chatter.getUserId());
            if (user != null) {
                users.add(user);
            } else {
                start(chatter.getUserId());
                users.add(userService.getUserByTwitchId(chatter.getUserId()));
            }
        }
        return users;
    }


    public void setSpawn(Boolean active, int cdMinutes, int maxCdMinutes) {
        if (spawn != null) {
            spawn.active = false;
            log.info("Spawn thread deactivated");
        }
        if (active) {
            spawn = new Spawn(this, cdMinutes, maxCdMinutes);
            spawn.start();
            log.info("Spawn thread started with cdMinutes: {} and maxCdMinutes: {}", cdMinutes, maxCdMinutes);
        } else {
            log.info("Stopping spawn");
        }
    }

    public void start (String twitchId) {
        User user = userService.getUserByTwitchId(twitchId);
        com.github.twitch4j.helix.domain.User twitchUser = getUserDetails(Integer.parseInt(twitchId));

        if (user == null) {
            User newUser = new User(twitchUser.getId(), twitchUser.getDisplayName().toLowerCase(), twitchUser.getProfileImageUrl());
            userService.saveUser(newUser);
            return;
        }

        if (!user.getUsername().equals(twitchUser.getDisplayName().toLowerCase()) || !user.getAvatar().equals(twitchUser.getProfileImageUrl())) {
            user.setUsername(twitchUser.getDisplayName().toLowerCase());
            user.setAvatar(twitchUser.getProfileImageUrl());
            userService.saveUser(user);
        }
    }

    public void followingReward(String twitchId) {
        start(twitchId);

        User user = userService.getUserByTwitchId(twitchId);

        /*
        sendMessage("Gracias por seguirme " + user.getUsernameDisplay() + "!");

        Items items = user.getItems();
        items.addSuperball();
        items.addSuperball(10);
        items.addUltraball(10);
         */

        userService.saveUser(user);
    }

    public void subReward(String twitchId){
        start(twitchId);

        User user = userService.getUserByTwitchId(twitchId);

        /*
        sendMessage("Gracias por suscribirte " + user.getUsernameDisplay() + "!");

        Items items = user.getItems();
        items.addSuperball(50);
        items.addUltraball(20);
        items.addMasterball(5);
         */

        userService.saveUser(user);
    }

    public com.github.twitch4j.helix.domain.User getUserDetails(String username) {
        com.github.twitch4j.helix.domain.User[] user = new com.github.twitch4j.helix.domain.User[1];

        twitchClient.getHelix().getUsers(null, null, List.of(username)).execute().getUsers().forEach(u -> {
            user[0] = u;
        });

        return user[0];
    }

    public com.github.twitch4j.helix.domain.User getUserDetails(int id) {
        com.github.twitch4j.helix.domain.User[] users = new com.github.twitch4j.helix.domain.User[1];

        twitchClient.getHelix().getUsers(null, List.of(String.valueOf(id)), null).execute().getUsers().forEach(user -> {
            users[0] = user;
        });

        return users[0];
    }

    public Achievement spawnPhoto() {
        log.info("Spawning photo");
        Achievement newAchievement = achievementGeneration.generateRandomAchievement();

        if (newAchievement != null) {
            wildAchievement = newAchievement;

            try {
                sendMessage("Ha aparecido el logro " + Utilities.firstLetterToUpperCase(wildAchievement.getName()) + " (" + wildAchievement.getGameName() + " " + wildAchievement.getRarity() + "% )!" );
                cardInfoClient.sendWildCard(wildAchievement);
                return wildAchievement;
            } catch (Exception e) {
                System.out.println("Error al enviar el sprite del pokemon");
            }
        }
        return null;
    }

    public boolean isLive() {
        return !twitchClient.getHelix().getStreams(settings.getTokenBot(), null, null, null, null, null, null, List.of(settings.getChannelName())).execute().getStreams().isEmpty();
    }

    public void returnRedemption(ChannelPointsRedemption redemption) {
        twitchClient.getHelix().updateRedemptionStatus(
                settings.getTokenChannel(),
                channel.getId(),
                redemption.getReward().getId(),
                List.of(redemption.getId()),
                RedemptionStatus.CANCELED
        ).execute();
    }

    public boolean catchPokemon(String idTwitch, Pokeballs pokeball) {
        start(idTwitch);

        if (wildAchievement == null) {
            sendMessage("No hay ningun logro!");
            return false;
        }

        int random = (int) (Math.random() * pokeball.catchRate) + 1;


        //wildAchievement.getRarity() is a percentage (0-100) ex 67.1
        int catchDifficulty = (int) (wildAchievement.getRarity() * 5);

        log.info(random  + " / " + catchDifficulty);

        boolean caught = random <= catchDifficulty;

        cardInfoClient.sendCatchPokemon(pokeball.toString(), caught);

        User user = userService.getUserByTwitchId(idTwitch);
        if (caught) {
            wildAchievement.setUser(user);
            achievementService.saveAchievement(wildAchievement);

            sendMessage(Utilities.firstLetterToUpperCase(user.getUsername()) + " ha conseguido el logro " + Utilities.firstLetterToUpperCase(wildAchievement.getName()) + "!");

            wildAchievement = null;
        } else {
            //sendMessage("La foto de " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " se le ha escapado de las manos a " + user.getUsername() + "!");
        }

        return true;
    }
}