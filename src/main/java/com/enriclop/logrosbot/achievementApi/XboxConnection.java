package com.enriclop.logrosbot.achievementApi;

import com.enriclop.logrosbot.dto.xboxDTO.XboxAchievementDTO;
import com.enriclop.logrosbot.dto.xboxDTO.XboxGameDTO;
import com.enriclop.logrosbot.security.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class XboxConnection {

    @Autowired
    Settings settings;

    String LENGUAGE_HEADER = "es-ES";

    public List<XboxGameDTO> getAllGames() {
        String url = "https://xbl.io/api/v2/achievements";

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("x-authorization", settings.getXboxKey());
            request.getHeaders().add("Accept-Language", LENGUAGE_HEADER);
            return execution.execute(request, body);
        });

        XboxResponseGamesDTO xboxResponseGamesDTO = restTemplate.getForObject(url, XboxResponseGamesDTO.class);

        if (xboxResponseGamesDTO == null) {
            return null;
        }

        List<XboxGameDTO> games = xboxResponseGamesDTO.titles;

        //games.removeIf(game -> game.achievement.currentAchievements == 0);
        games.removeIf(game -> game.achievement.progressPercentage != 100);

        return games;
    }

    static class XboxResponseGamesDTO {
        public String xuid;
        public List<XboxGameDTO> titles;
    }

    public List<XboxAchievementDTO> getAchievements(String titleId) {
        String url = "https://xbl.io/api/v2/achievements/title/" + titleId;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("x-authorization", settings.getXboxKey());
            request.getHeaders().add("Accept-Language", LENGUAGE_HEADER);
            return execution.execute(request, body);
        });

        XboxResponseAchievementsDTO xboxResponseAchievementsDTO = restTemplate.getForObject(url, XboxResponseAchievementsDTO.class);

        if (xboxResponseAchievementsDTO == null) {
            return null;
        }

        List<XboxAchievementDTO> achievements = xboxResponseAchievementsDTO.achievements;

        achievements.removeIf(achievement -> !achievement.progressState.equals("Achieved"));

        return achievements;
    }

    static class XboxResponseAchievementsDTO {
        public List<XboxAchievementDTO> achievements;
    }


}
