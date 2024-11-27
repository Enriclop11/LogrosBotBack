package com.enriclop.logrosbot.achievementApi;

import com.enriclop.logrosbot.dto.steamDTO.SteamGameDTO;
import com.enriclop.logrosbot.dto.steamDTO.SteamListAchievementsDTO;
import com.enriclop.logrosbot.dto.steamDTO.SteamListGamesDTO;
import com.enriclop.logrosbot.security.Settings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class SteamConnection {

    @Autowired
    Settings settings;

    public List<SteamGameDTO> getAllGames() {
        String ulr = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + settings.getSteamKey() + "&steamid=" + settings.getSteamId() + "&format=json&include_played_free_games=1";

        RestTemplate restTemplate = new RestTemplate();
        SteamResponseGamesDTO steamResponseGamesDTO = restTemplate.getForObject(ulr, SteamResponseGamesDTO.class);

        if (steamResponseGamesDTO == null) {
            return null;
        }

        SteamListGamesDTO steamListGamesDTO = steamResponseGamesDTO.response;

        List<SteamGameDTO> games = steamListGamesDTO.games;
        games.removeIf(game -> game.playtime_forever == 0);

        return games;
    }

    static class SteamResponseGamesDTO {
        public SteamListGamesDTO response;
    }

    public SteamListAchievementsDTO getAchievements(String appId) {
        String url = "http://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/?appid=" + appId + "&key=" + settings.getSteamKey() + "&steamid=" + settings.getSteamId() + "&l=spanish";

        RestTemplate restTemplate = new RestTemplate();
        SteamResponseAchievementsDTO steamResponseAchievementsDTO = restTemplate.getForObject(url, SteamResponseAchievementsDTO.class);

        if (steamResponseAchievementsDTO == null) {
            return null;
        }

        SteamListAchievementsDTO steamListAchievementsDTO = steamResponseAchievementsDTO.playerstats;

        //if the user don't have all the achievements, the response will be null
        if (steamListAchievementsDTO.achievements.stream().anyMatch(achievement -> achievement.achieved == 0)) {
            return null;
        }

        return steamListAchievementsDTO;
    }

    static class SteamResponseAchievementsDTO {
        public SteamListAchievementsDTO playerstats;
    }

    public double getAchievementPercentage(String appId, String achievementId) {
        String url = "https://api.steampowered.com/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/?gameid=" + appId + "&format=json";

        RestTemplate restTemplate = new RestTemplate();
        SteamResponseAchievementPercentageDTO steamResponseAchievementPercentageDTO = restTemplate.getForObject(url, SteamResponseAchievementPercentageDTO.class);

        if (steamResponseAchievementPercentageDTO == null) {
            return 0;
        }

        SteamListAchievementPercentageDTO steamListAchievementPercentageDTO = steamResponseAchievementPercentageDTO.achievementpercentages;
        List<SteamAchievementPercentageDTO> achievements = steamListAchievementPercentageDTO.achievements;

        for (SteamAchievementPercentageDTO achievement : achievements) {
            if (achievement.name.equals(achievementId)) {
                return achievement.percent;
            }
        }

        return 0;
    }

    static class SteamResponseAchievementPercentageDTO {
        public SteamListAchievementPercentageDTO achievementpercentages;
    }

    static class SteamListAchievementPercentageDTO {
        public List<SteamAchievementPercentageDTO> achievements;
    }

    static class SteamAchievementPercentageDTO {
        public String name;
        public double percent;
    }

    public String getAchievementIcon(String appId, String achievementId) {
        String url = "https://api.steampowered.com/ISteamUserStats/GetSchemaForGame/v0002/?appid=" + appId + "&format=json&key=" + settings.getSteamKey();

        RestTemplate restTemplate = new RestTemplate();
        SteamResponseIconDTO steamResponseIconDTO = restTemplate.getForObject(url, SteamResponseIconDTO.class);

        if (steamResponseIconDTO == null) {
            return null;
        }

        List<SteamIconDTO> achievements = steamResponseIconDTO.game.availableGameStats.achievements;

        for (SteamIconDTO achievement : achievements) {
            if (achievement.name.equals(achievementId)) {
                return achievement.icon;
            }
        }

        return null;

    }

    static class SteamResponseIconDTO {
        public SteamListGameStatsDTO game;
    }

    static class SteamListGameStatsDTO {
        public SteamGameStatsDTO availableGameStats;
        public String gameName;
        public String gameVersion;
    }

    static class SteamGameStatsDTO {
        public List<SteamIconDTO> achievements;
    }

    static class SteamIconDTO {
        public String name;
        public String icon;
    }

}
