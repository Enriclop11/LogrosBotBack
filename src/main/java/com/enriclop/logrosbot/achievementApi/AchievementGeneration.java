package com.enriclop.logrosbot.achievementApi;

import com.enriclop.logrosbot.dto.steamDTO.SteamAchievementDTO;
import com.enriclop.logrosbot.dto.steamDTO.SteamGameDTO;
import com.enriclop.logrosbot.dto.steamDTO.SteamListAchievementsDTO;
import com.enriclop.logrosbot.modelo.Achievement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AchievementGeneration {

    @Autowired
    private SteamConnection steamConnection;

    public Achievement generateSteamRandomAchievement() {
        try {
            List<SteamGameDTO> achievements = steamConnection.getAllGames();
            SteamGameDTO randomGame = achievements.get((int) (Math.random() * achievements.size()));
            SteamListAchievementsDTO gameAchievementDTO = steamConnection.getAchievements(randomGame.appid);

            if (gameAchievementDTO == null) {
                return generateSteamRandomAchievement();
            }

            List<SteamAchievementDTO> gameAchievements = gameAchievementDTO.achievements;

            if (gameAchievements.isEmpty()) {
                return generateSteamRandomAchievement();
            }

            SteamAchievementDTO randomAchievement = gameAchievements.get((int) (Math.random() * gameAchievements.size()));
            Achievement newAchievement = new Achievement();
            newAchievement.setName(randomAchievement.name);
            newAchievement.setDescription(randomAchievement.description);
            newAchievement.setAchievementId(randomAchievement.apiname);
            newAchievement.setGameId(gameAchievementDTO.steamID);
            newAchievement.setGameName(gameAchievementDTO.gameName);

            String photo = steamConnection.getAchievementIcon(randomGame.appid, randomAchievement.apiname);
            newAchievement.setPhoto(photo);

            double rarity = steamConnection.getAchievementPercentage(randomGame.appid, randomAchievement.apiname);
            newAchievement.setRarity(rarity);

            newAchievement.setPlatform("steam");

            return newAchievement;
        } catch (Exception e) {
            return generateSteamRandomAchievement();
        }
    }
}
