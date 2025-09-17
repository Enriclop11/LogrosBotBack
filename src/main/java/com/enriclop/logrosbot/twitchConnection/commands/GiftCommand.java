package com.enriclop.logrosbot.twitchConnection.commands;


import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class GiftCommand extends Command {

    public GiftCommand() {
        super(
                "Gift",
                "!gift",
                "Regala una logro a otro usuario",
                true,
                false,
                0,
                0
        );
    }

    @Override
    public void execute(TwitchConnection connection, ChannelMessageEvent event) {
        User user = connection.getUserService().getUserByTwitchId(event.getUser().getId());

        if (event.getMessage().split(" ").length < 3) {
            connection.sendMessage("Elige un usuario y un logro para regalar!");
            return;
        }

        String username = event.getMessage().split(" ")[1];

        String cardIndex = event.getMessage().split(" ")[2];

        if (username.startsWith("@")) {
            username = username.substring(1);
        }

        User receiver = connection.getUserService().getUserByUsername(username);

        if (receiver == null) {
            connection.sendMessage("El usuario no existe!");
            return;
        }

        int index;

        try {
            index = Integer.parseInt(cardIndex);
        } catch (Exception e) {
            connection.sendMessage("Elige un logro para regalar!");
            return;
        }

        if (index < 1 || index > user.getAchievements().size()) {
            connection.sendMessage("Elige un logro para regalar!");
            return;
        }

        Achievement achievement = user.getAchievements().get(index - 1);

        if (achievement.getUser() != user) {
            connection.sendMessage("No puedes regalar un logro que no es tuyo!");
            return;
        }

        achievement.setUser(receiver);
        connection.getAchievementService().saveAchievement(achievement);

        connection.sendMessage(user.getUsernameDisplay() + " ha regalado a " + receiver.getUsernameDisplay() + " el logro de " + achievement.getName() + "!");
    }
}
