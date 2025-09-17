package com.enriclop.logrosbot.twitchConnection.rewards;

import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import static com.enriclop.logrosbot.enums.Pokeballs.POKEBALL;


public class CatchReward extends Reward {

    public CatchReward() {
        super(
                "Catch",
                "catch",
                10,
                true,
                false,
                0
        );
    }

    @Override
    public void execute(TwitchConnection connection, RewardRedeemedEvent event) {
        if(!connection.catchPokemon(event.getRedemption().getUser().getId(), POKEBALL)) {
            returnReedemption(connection, event);
        }
    }
}
