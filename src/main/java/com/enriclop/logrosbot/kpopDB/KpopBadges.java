package com.enriclop.logrosbot.kpopDB;

import com.enriclop.logrosbot.dto.BadgeDTO;
import com.enriclop.logrosbot.dto.BadgeListDTO;
import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.modelo.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class KpopBadges {

    public static User checkBadges(User user) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = KpopBadges.class.getResourceAsStream("/kpopData/badges.json");) {

            BadgeListDTO badgeList = mapper.readValue(inputStream, BadgeListDTO.class);

            for (BadgeDTO badge : badgeList.getBadges()) {
                if (user.getBadges().stream().anyMatch(b -> b.getName().equals(badge.getName()))) {
                    continue;
                }

                if (checkRequirements(user, badge)) {
                    user.addBadge(badge);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return user;
    }

    public static boolean checkRequirements(User user, BadgeDTO badge) {

        List<String> requirements = new ArrayList<>(List.of(badge.getRequirements()));

        // The String list are the idolIds that the user has to have in order to get the badge

        for (Achievement card : user.getAchievements()) {
            requirements.remove(card.getAchievementId());

            if (requirements.isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
