package com.enriclop.logrosbot.achievementApi;

import com.enriclop.logrosbot.dto.steamDTO.SteamAchievementDTO;
import com.enriclop.logrosbot.dto.steamDTO.SteamGameDTO;
import com.enriclop.logrosbot.dto.steamDTO.SteamListAchievementsDTO;
import com.enriclop.logrosbot.dto.xboxDTO.XboxAchievementDTO;
import com.enriclop.logrosbot.dto.xboxDTO.XboxGameDTO;
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

    @Autowired
    private XboxConnection xboxConnection;

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

            String photo = steamConnection.getAchievementIcon(randomGame.appid, randomAchievement.apiname);

            double rarity = steamConnection.getAchievementPercentage(randomGame.appid, randomAchievement.apiname);

            return new Achievement(randomAchievement, gameAchievementDTO, photo, rarity);
        } catch (Exception e) {
            return generateSteamRandomAchievement();
        }
    }

    public Achievement generateXboxRandomAchievement() {
        try {
            List<XboxGameDTO> games = xboxConnection.getAllGames();
            XboxGameDTO randomGame = games.get((int) (Math.random() * games.size()));
            List<XboxAchievementDTO> achievements = xboxConnection.getAchievements(randomGame.titleId);

            if (achievements == null) {
                return generateXboxRandomAchievement();
            }

            if (achievements.isEmpty()) {
                return generateXboxRandomAchievement();
            }

            XboxAchievementDTO randomAchievement = achievements.get((int) (Math.random() * achievements.size()));

            return new Achievement(randomGame, randomAchievement);
        } catch (Exception e) {
            return generateXboxRandomAchievement();
        }
    }

    public Achievement generateRandomAchievement() {
        return generateSteamRandomAchievement();
    }
}
