package com.enriclop.logrosbot.twitchConnection.commands;

import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class InventoryCommand extends Command {

    public InventoryCommand() {
        super(
                "Inventory",
                "!logros",
                "Enseña los logros que tienes",
                true,
                false,
                0,
                0
        );
    }

    @Override
    public void execute(TwitchConnection connection, ChannelMessageEvent event) {
        connection.sendMessage("Tus Logros: " + connection.getSettings().getDomain() + "/user/" + event.getUser().getName().toLowerCase());
    }


}
