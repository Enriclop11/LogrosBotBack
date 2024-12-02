package com.enriclop.logrosbot.modelo;

import com.enriclop.logrosbot.dto.steamDTO.SteamAchievementDTO;
import com.enriclop.logrosbot.dto.steamDTO.SteamListAchievementsDTO;
import com.enriclop.logrosbot.dto.xboxDTO.XboxAchievementDTO;
import com.enriclop.logrosbot.dto.xboxDTO.XboxGameDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "achievements")
@Data
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String achievementId;

    private String name;

    private String description;

    private String photo;

    private double rarity;

    private String gameName;

    private String gameId;

    private String platform;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public Achievement() {
    }

    public Achievement(Achievement achievement) {
        this.achievementId = achievement.getAchievementId();
        this.name = achievement.getName();
        this.description = achievement.getDescription();
        this.photo = achievement.getPhoto();
        this.rarity = achievement.getRarity();
        this.gameName = achievement.getGameName();
        this.gameId = achievement.getGameId();
        this.platform = achievement.getPlatform();
    }

    public Achievement(SteamAchievementDTO achievementDTO, SteamListAchievementsDTO gameAchievementDTO, String photo, double rarity) {
        this.achievementId = achievementDTO.apiname;
        this.name = achievementDTO.name;
        this.description = achievementDTO.description;
        this.gameName = gameAchievementDTO.gameName;
        this.gameId = gameAchievementDTO.steamID;
        this.platform = "steam";
        this.photo = photo;
        this.rarity = Math.round(rarity * 100.0) / 100.0;
    }

    public Achievement(XboxGameDTO gameDTO, XboxAchievementDTO achievementDTO) {
        this.achievementId = achievementDTO.id;
        this.name = achievementDTO.name;
        this.description = achievementDTO.description;
        this.gameName = gameDTO.name;
        this.gameId = gameDTO.titleId;
        this.platform = "xbox";
        this.photo = achievementDTO.mediaAssets.get(0).url;
        this.rarity = Math.round(achievementDTO.rarity.currentPercentage * 100.0) / 100.0;
    }


}
