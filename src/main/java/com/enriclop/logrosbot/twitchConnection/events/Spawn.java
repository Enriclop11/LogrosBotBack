package com.enriclop.logrosbot.twitchConnection.events;

import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Spawn extends Event {

    Achievement wildCard;

    public Spawn() {
        super(
                "Spawn",
                false,
                5,
                30);
    }

    @Override
    protected void execute(TwitchConnection connection) {
            wildCard = connection.spawnPhoto();
            log.info("Card spawned: " + wildCard.getName());
    }

}
