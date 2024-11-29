package com.enriclop.logrosbot.twitchConnection;

import com.enriclop.logrosbot.achievementApi.AchievementGeneration;
import com.enriclop.logrosbot.dto.Command;
import com.enriclop.logrosbot.enums.Pokeballs;
import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.modelo.Items;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.security.Settings;
import com.enriclop.logrosbot.servicio.AchievementService;
import com.enriclop.logrosbot.servicio.ItemsService;
import com.enriclop.logrosbot.servicio.UserService;
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
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.Chatter;
import com.github.twitch4j.helix.domain.ChattersList;
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.github.twitch4j.util.PaginationUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.enriclop.logrosbot.enums.Pokeballs.*;

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

    List<Command> rewards;

    OAuth2Credential streamerCredential;

    com.github.twitch4j.helix.domain.User channel;

    @Autowired
    private AchievementGeneration achievementGeneration;

    public TwitchConnection() {
        commands = List.of(
                new Command("leaderboard", true),
                new Command("spawn", true),
                new Command("catch", true),
                new Command("combat", true),
                new Command("myphotos", true),
                new Command("refreshusername", false),
                new Command("buy", false),
                new Command("items", false),
                new Command("points", true),
                new Command("help", true),
                new Command("lookprices", false),
                new Command("linkdiscord", false),
                new Command("watchtime", true),
                new Command("select", true),
                new Command("trade", true),
                new Command("gift", true),
                new Command("password", true)
        );

        rewards = List.of(
                new Command("catch", true),
                new Command("superball", true),
                new Command("ultraball", true),
                new Command("masterball", true),
                new Command("test", true)
        );
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
         twitchClient.getPubSub().listenForSubscriptionEvents(streamerCredential, channel.getId());
         
         twitchClient.getClientHelper().enableFollowEventListener(settings.getChannelName());

         commands();

         new SetWatchTime(this).start();

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
            if (!event.getMessage().startsWith("!")) return;

            String command = event.getMessage().split(" ")[0];
            command = command.substring(1);
            String finalCommand = command.toLowerCase();
            useCommand(commands.stream().filter(c -> c.getCustomName().equals(finalCommand)).findFirst().orElse(null), event);
        });

        eventManager.onEvent(RewardRedeemedEvent.class, event -> {
            String reward = event.getRedemption().getReward().getTitle();

            System.out.println(reward);

            String finalReward = reward.toLowerCase();
            useReward(rewards.stream().filter(r -> r.getCustomName().equals(finalReward)).findFirst().orElse(null), event);
        });

        eventManager.onEvent(FollowEvent.class , event -> {
            followingReward(event.getUser().getId());
        });

        eventManager.onEvent(ChannelSubscribeEvent.class, event -> {
            subReward(event.getData().getUserId());
        });

    }

    public void useCommand(Command command, ChannelMessageEvent event) {

        if (command == null) return;
        if (!command.isActive()) return;

        switch (command.getName()) {
            case "help" -> help();
            case "leaderboard" -> leaderboard();
            case "spawn" -> spawnPhoto();
            case "catch" -> trowPokeball(event);
            case "myphotos" -> lookAlbum(event);
            case "refreshusername" -> refreshUsername(event.getUser());
            //case "buy" -> buyItem(event);
            //case "items" -> lookItems(event);
            case "points" -> myPoints(event);
            //case "lookprices" -> lookPrices(event);
            case "watchtime" -> getWatchtime(event);
            case "select" -> selectPokemon(event);
            case "trade" -> startTrade(event);
            case "gift" -> gift(event);
            case "password" -> passwordChange(event);
        }
    }

    public void help() {
        // Get the list of commands that are active and send them to the chat
        StringBuilder help = new StringBuilder("Comandos disponibles: ");
        for (Command command : commands) {
            if (command.isActive()) {
                help.append("!" + command.getCustomName() + " ");
            }
        }
        sendMessage(help.toString());
    }

    public void useReward(Command command, RewardRedeemedEvent event){
        if (command == null) return;
        if (!command.isActive()) return;

        switch (command.getName()) {
            case "catch" -> {
                System.out.println("catching");
                catchPokemon(event.getRedemption().getUser().getId(), POKEBALL);
            }
            case "superball" -> {
                catchPokemon(event.getRedemption().getUser().getId(), SUPERBALL);
            }
            case "ultraball" -> {
                catchPokemon(event.getRedemption().getUser().getId(), ULTRABALL);
            }
            case "masterball" -> {
                catchPokemon(event.getRedemption().getUser().getId(), MASTERBALL);
            }
            //dox the user ip
            case "test" -> sendMessage("https://www.youtube.com/watch?v=BbeeuzU5Qc8");
        }
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
        if (userService.getUserByTwitchId(twitchId) == null) {
            com.github.twitch4j.helix.domain.User user = getUserDetails(Integer.parseInt(twitchId));
            User newUser = new User(user.getId(), user.getDisplayName().toLowerCase(), user.getProfileImageUrl());
            userService.saveUser(newUser);
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

    public void refreshUsername (EventUser sender) {
        try {
            User user = userService.getUserByTwitchId(sender.getId());
            if (user != null && !user.getUsername().equals(sender.getName().toLowerCase())) {
                user.setUsername(sender.getName().toLowerCase());
                userService.saveUser(user);
            }
        } catch (Exception e) {
            start(sender.getId());
        }
    }

    public void leaderboard() {
        /*
        List<User> users = userService.getUsers();

        users.sort((u1, u2) -> u2.getPhotoCards().size() - u1.getPhotoCards().size());

        if (users.size() > 10) {
            users = users.subList(0, 10);
        }

        StringBuilder leaderboard = new StringBuilder("Leaderboard: ");
        for (User user : users) {
            leaderboard.append ((users.indexOf(user) + 1) + ". " + user.getUsername() + " " + user.getPhotoCards().size() + " Pokemon ");
        }

        twitchClient.getChat().sendMessage(settings.channelName,leaderboard.toString());

         */

        //Send a message to the chat with the url of the leaderboard
        sendMessage("Leaderboard: " + settings.getDomain() + "/leaderboard");
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

    public void trowPokeball(ChannelMessageEvent event){
        start(event.getUser().getId());

        if (wildAchievement != null) {

            String pokeball;

            try {
                pokeball = event.getMessage().split(" ")[1];
            } catch (Exception e) {
                pokeball = "pokeball";
            }

            Items pokeballs = userService.getUserByTwitchId(event.getUser().getId()).getItems();

            switch (pokeball) {
                case "superball" -> {
                    if (pokeballs.getSuperball() > 0) {
                        pokeballs.useSuperball();
                        itemsService.saveItem(pokeballs);
                        catchPokemon(event.getUser().getId(), SUPERBALL);
                    } else {
                        sendMessage("No tienes Super Balls!");
                    }
                }
                case "ultraball" -> {
                    if (pokeballs.getUltraball() > 0) {
                        pokeballs.useUltraball();
                        itemsService.saveItem(pokeballs);
                        catchPokemon(event.getUser().getId(), ULTRABALL);
                    } else {
                        sendMessage("No tienes Ultra Balls!");
                    }
                }
                case "masterball" -> {
                    if (pokeballs.getMasterball() > 0) {
                        pokeballs.useMasterball();
                        itemsService.saveItem(pokeballs);
                        catchPokemon(event.getUser().getId(), MASTERBALL);
                    } else {
                        sendMessage("No tienes Master Balls!");
                    }
                }
                default -> catchPokemon(event.getUser().getId(), POKEBALL);
            }

        } else {
            sendMessage("No hay ningun logro!");
        }
    }

    public void catchPokemon(String idTwitch, Pokeballs pokeball) {
        start(idTwitch);

        if (wildAchievement == null) {
            sendMessage("No hay ningun logro!");
            return;
        }

        int random = (int) (Math.random() * pokeball.catchRate) + 1;


        double catchDifficulty = wildAchievement.getRarity();

        boolean caught = random < catchDifficulty;

        log.info("Catch difficulty: " + catchDifficulty + " Random: " + random + " Caught: " + caught);

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

    }

    @Transactional
    public List<Achievement> getSelectedPokemons(User user) {
        try {
            List<Achievement> selectedPokemons = new ArrayList<>();
            log.info("Selected cards: " + user.getSelectedCards());
            for (Integer id : user.getSelectedCards()) {
                log.info("Selected card: " + id);
                selectedPokemons.add(achievementService.getAchievementById(id));
            }
            return selectedPokemons;
        } catch (Exception e) {
            log.error("Error getting selected pokemons", e);
            return new ArrayList<>();
        }
    }

    public void lookAlbum(ChannelMessageEvent event){
        start(event.getUser().getId());
        sendMessage("Tus photocards: " + settings.getDomain() + "/photocards/" + event.getUser().getName().toLowerCase());
    }

    public void lookPrices (ChannelMessageEvent event){
        start(event.getUser().getId());
        sendMessage("Precios: Superball: " + prices.getSuperballPrice() + " Ultraball: " + prices.getUltraballPrice() + " Masterball: " + prices.getMasterballPrice());
    }

    public void buyItem(ChannelMessageEvent event) {
        start(event.getUser().getId());

        String item = event.getMessage().split(" ")[1];

        int amount;
        try{
            amount = Integer.parseInt(event.getMessage().split(" ")[2]);
            if (amount < 1) amount = 1;
            if (amount > 100) amount = 100;
        } catch (Exception e) {
            amount = 1;
        }

        User user = userService.getUserByTwitchId(event.getUser().getId());

        Items items = user.getItems();

        switch (item) {
            case "superball" -> {
                if (user.getScore() >= (prices.getSuperballPrice() * amount)) {
                    items.addSuperball(amount);
                    user.minusScore(prices.getSuperballPrice() * amount);
                    itemsService.saveItem(items);
                    userService.saveUser(user);
                    sendMessage("Has comprado una Superball!");
                } else {
                    sendMessage("No tienes suficiente dinero!");
                }
            }
            case "ultraball" -> {
                if (user.getScore() >= (prices.getUltraballPrice() * amount)) {
                    items.addUltraball(amount);
                    user.minusScore(prices.getUltraballPrice() * amount);
                    itemsService.saveItem(items);
                    userService.saveUser(user);
                    sendMessage("Has comprado una Ultraball!");
                } else {
                    sendMessage("No tienes suficiente dinero!");
                }
            }
            case "masterball" -> {
                if (user.getScore() >= (prices.getMasterballPrice() * amount)) {
                    items.addMasterball(amount);
                    user.minusScore(prices.getMasterballPrice() * amount);
                    itemsService.saveItem(items);
                    userService.saveUser(user);
                    sendMessage("Has comprado una Masterball!");
                } else {
                    sendMessage("No tienes suficiente dinero!");
                }
            }
            default -> sendMessage("El item no existe!");
        }
    }

    public void lookItems(ChannelMessageEvent event) {
        start(event.getUser().getId());

        User user = userService.getUserByTwitchId(event.getUser().getId());

        Items items = user.getItems();

        sendMessage("Tus items: Superball: " + items.getSuperball() + " Ultraball: " + items.getUltraball() + " Masterball: " + items.getMasterball());
    }


    public void myPoints(ChannelMessageEvent event) {
        start(event.getUser().getId());

        User user = userService.getUserByTwitchId(event.getUser().getId());

        sendMessage("Tienes " + user.getScore() + " puntos!");
    }

    public void selectPokemon(ChannelMessageEvent event) {
        start(event.getUser().getId());

        User user = userService.getUserByTwitchId(event.getUser().getId());

        if (event.getMessage().split(" ").length < 2) {
            sendMessage("Elige una carta para seleccionar!");
            return;
        }

        String pokemonPos = event.getMessage().split(" ")[1];
        int pos;

        try {
            pos = Integer.parseInt(pokemonPos);
        } catch (Exception e) {
            sendMessage("Elige una carta para seleccionar!");
            return;
        }

        if (pos < 1 || pos > user.getAchievements().size()) {
            sendMessage("Elige una carta para seleccionar!");
            return;
        }

        Achievement pokemon = user.getAchievements().get(pos - 1);

        List<Integer> selectedCards = user.getSelectedCards();

        if (selectedCards.contains(pokemon.getId())) {
            sendMessage("Ya has seleccionado a " + Utilities.firstLetterToUpperCase(pokemon.getName()) + "!");
            return;
        }

        if (selectedCards.size() >= 3) {
            selectedCards.remove(0);
        }

        selectedCards.add(pokemon.getId());

        user.setSelectedCards(selectedCards);

        userService.saveUser(user);

        sendMessage(user.getUsernameDisplay() + " ha seleccionado a " + Utilities.firstLetterToUpperCase(pokemon.getName()) + "!");
    }

    public void startTrade(ChannelMessageEvent event){
        //!trade @user photocardIndex

        User user = userService.getUserByTwitchId(event.getUser().getId());

        if (event.getMessage().split(" ").length < 3) {
            sendMessage("Elige un usuario y una carta para intercambiar!");
            return;
        }

        String username = event.getMessage().split(" ")[1];
        String pokemonIndex = event.getMessage().split(" ")[2];

        if (username.startsWith("@")) {
            username = username.substring(1);
        }

        User receiver = userService.getUserByUsername(username);

        if (receiver == null) {
            sendMessage("El usuario no existe!");
            return;
        }

        int index;

        try {
            index = Integer.parseInt(pokemonIndex);
        } catch (Exception e) {
            sendMessage("Elige una foto para intercambiar!");
            return;
        }

        if (index < 1 || index > user.getAchievements().size()) {
            sendMessage("Elige una foto para intercambiar!");
            return;
        }

        Achievement pokemon = user.getAchievements().get(index - 1);

        if (pokemon.getUser() != user) {
            sendMessage("No puedes intercambiar un pokemon que no es tuyo!");
            return;
        }

        if (currentTrade != null) currentTrade.stopTrade();
        currentTrade = new Trade(user, receiver, pokemon, userService, achievementService, twitchClient, this);
        currentTrade.start();
    }

    public void gift(ChannelMessageEvent event){
        //!gift @user photocardIndex

        User user = userService.getUserByTwitchId(event.getUser().getId());

        if (event.getMessage().split(" ").length < 3) {
            sendMessage("Elige un usuario y una carta para regalar!");
            return;
        }

        String username = event.getMessage().split(" ")[1];
        String pokemonIndex = event.getMessage().split(" ")[2];

        if (username.startsWith("@")) {
            username = username.substring(1);
        }

        User receiver = userService.getUserByUsername(username);

        if (receiver == null) {
            sendMessage("El usuario no existe!");
            return;
        }

        int index;

        try {
            index = Integer.parseInt(pokemonIndex);
        } catch (Exception e) {
            sendMessage("Elige una carta para regalar!");
            return;
        }

        if (index < 1 || index > user.getAchievements().size()) {
            sendMessage("Elige una carta para regalar!");
            return;
        }

        Achievement pokemon = user.getAchievements().get(index - 1);

        if (pokemon.getUser() != user) {
            sendMessage("No puedes regalar una carta que no es tuya!");
            return;
        }

        pokemon.setUser(receiver);
        achievementService.saveAchievement(pokemon);

        //        sendMessage("Has regalado a " + Utilities.firstLetterToUpperCase(receiver.getUsername()) + " la foto de " + Utilities.firstLetterToUpperCase(pokemon.getName()) + "!");
        sendMessage(user.getUsernameDisplay() + " ha regalado a " + receiver.getUsernameDisplay() + " la foto de " + Utilities.firstLetterToUpperCase(pokemon.getName()) + "!");
    }

    public void passwordChange(ChannelMessageEvent event) {
        //When the user asks for a password change, send a whisper to the user with the new password
        //The user must be registered in the database

        log.info("Password change " + event.getUser().getName());

        User user = userService.getUserByTwitchId(event.getUser().getId());

        if (user == null) {
            sendMessage("No estás registrado en la base de datos!");
            return;
        }

        String newPassword = Utilities.generatePassword();

        user.setPassword(newPassword);
        log.info("Changing password for user: " + user.getUsername() + " to " + newPassword);
        userService.saveUser(user);

        try{
            //NOTE: The API may silently drop whispers that it suspects of violating Twitch policies. (The API does not indicate that it dropped the whisper; it returns a 204 status code as if it succeeded.)
            sendWhisper(user.getTwitchId(), "Tu nueva contraseña es: " + newPassword);
        } catch (Exception e) {
            sendMessage("No se ha podido enviar la contraseña a tu mensaje privado!");
        }
    }

    public void getWatchtime(ChannelMessageEvent event) {
        User user = userService.getUserByTwitchId(event.getUser().getId());
        if (user.getWatchTime().getDays() > 0) {
            sendMessage(event.getUser().getName() + "ha pasado " + (int) user.getWatchTime().getDays() + " dias viendo el stream!");
        } else if (user.getWatchTime().getHours() > 0) {
            sendMessage(event.getUser().getName() + "ha pasado " + (int) user.getWatchTime().getHours() + " horas viendo el stream!");
        } else {
            sendMessage(event.getUser().getName() + "ha pasado " + (int) user.getWatchTime().getMinutes() + " minutos viendo el stream!");
        }
    }

    public boolean isLive() {
        return !twitchClient.getHelix().getStreams(settings.getTokenBot(), null, null, null, null, null, null, List.of(settings.getChannelName())).execute().getStreams().isEmpty();
    }
}