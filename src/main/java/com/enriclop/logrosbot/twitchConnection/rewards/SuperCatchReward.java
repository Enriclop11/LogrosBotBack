package com.enriclop.logrosbot.twitchConnection.rewards;

import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import static com.enriclop.logrosbot.enums.Pokeballs.MASTERBALL;


public class SuperCatchReward extends Reward{

    public SuperCatchReward() {
        super(
                "SuperCatch",
                "SuperCatch",
                1000,
                false,
                false,
                0
        );
    }

    @Override
    public void execute(TwitchConnection connection, RewardRedeemedEvent event) {
        if(!connection.catchPokemon(event.getRedemption().getUser().getId(), MASTERBALL)) {
            returnReedemption(connection, event);
        }
    }
}
