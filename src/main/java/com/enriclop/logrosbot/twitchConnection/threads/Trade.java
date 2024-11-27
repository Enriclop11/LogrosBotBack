package com.enriclop.logrosbot.twitchConnection.threads;

import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.servicio.AchievementService;
import com.enriclop.logrosbot.servicio.UserService;
import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class Trade extends Thread {

    UserService userService;
    AchievementService photoAchievementService;
    TwitchClient twitchClient;
    TwitchConnection conn;
    User user1;
    User user2;
    Achievement card1;
    Achievement card2;
    boolean offer = false;
    boolean accepted = false;
    boolean stopped = false;

    public Trade(User user, User receiver, Achievement pokemon, UserService userService, AchievementService photoAchievementService, TwitchClient twitchClient, TwitchConnection conn) {
        this.user1 = user;
        this.user2 = receiver;
        this.card1 = pokemon;
        this.userService = userService;
        this.photoAchievementService = photoAchievementService;
        this.twitchClient = twitchClient;
        this.conn = conn;
    }

    @Override
    public void run() {
        if (stopped) return;

        sendMessage(user1.getUsername() + " quiere intercambiar su " + card1.getName() + " por una de tus cartas. ¿Qué carta quieres darle? !offer [numero de carta]");

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            if (offer || stopped) return;
            if (!event.getMessage().startsWith("!offer")) return;

            if (event.getUser().getName().equals(user2.getUsername())) {
                String[] message = event.getMessage().split(" ");
                int cardId = Integer.parseInt(message[1]);
                card2 = user2.getAchievements().get(cardId - 1);

                if (card2 != null) {
                    offer = true;
                    sendMessage(user2.getUsername() + " ha ofrecido su " + card2.getName() + " por tu " + card1.getName() + ". ¿Aceptas? !accept");

                    acceptOffer();
                } else {
                    sendMessage("No tienes una carta con ese número. Por favor, introduce un número válido.");
                }
            }
        });
    }

    public void acceptOffer() {
        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            if (accepted || stopped) return;
            if (!event.getMessage().startsWith("!accept")) return;

            if (event.getUser().getName().equals(user1.getUsername())) {
                accepted = true;
                photoAchievementService.changeUser(card1, user2);
                photoAchievementService.changeUser(card2, user1);

                sendMessage(user1.getUsername() + " ha aceptado tu oferta. ¡Intercambio completado!");
            }
        });
    }

    public void stopTrade() {
        stopped = true;
    }

    public void sendMessage(String message) {
        twitchClient.getChat().sendMessage(conn.getSettings().getChannelName(), message);
    }
}