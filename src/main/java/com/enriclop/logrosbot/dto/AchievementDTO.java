package com.enriclop.logrosbot.dto;

import com.enriclop.logrosbot.modelo.Achievement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AchievementDTO {

    private int id;

    private String achievementId;

    private String name;

    private String description;

    private String photo;

    private double rarity;

    private String gameName;

    private String gameId;

    private String platform;

    AchievementDTO(Achievement card) {
        this.id = card.getId();
        this.achievementId = card.getAchievementId();
        this.name = card.getName();
        this.description = card.getDescription();
        this.photo = card.getPhoto();
        this.rarity = card.getRarity();
        this.gameName = card.getGameName();
        this.gameId = card.getGameId();
        this.platform = card.getPlatform();
    }

    public static List<AchievementDTO> fromPhotoCards(List<Achievement> achievements) {
        return achievements.stream().map(AchievementDTO::new).toList();
    }
}
