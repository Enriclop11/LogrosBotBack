package com.enriclop.logrosbot.dto.user;

import com.enriclop.logrosbot.dto.AchievementDTO;
import com.enriclop.logrosbot.modelo.Aura;
import com.enriclop.logrosbot.modelo.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserCardsDTO {

    private int id;

    private String twitchId;

    private String username;

    private Integer score;

    private String avatar;

    private List<AchievementDTO> achievements;

    private List<Aura> auras;

    //private AchievementDTO selectedAchievement;

    public UserCardsDTO(User user) {
        this.id = user.getId();
        this.twitchId = user.getTwitchId();
        this.username = user.getUsername();
        this.score = user.getScore();
        this.avatar = user.getAvatar();
        this.achievements = AchievementDTO.fromPhotoCards(user.getAchievements());
        this.auras = user.getAuras();


        /*
        this.selectedAchievement = user.getSelectedAchievement() != null ?
                this.achievements.stream()
                        .filter(a -> a.getId() == user.getSelectedAchievement())
                        .findFirst()
                        .orElse(null) : null;

         */
    }

    public static List<UserCardsDTO> fromUsers(List<User> users) {
        return users.stream().map(UserCardsDTO::new).toList();
    }
}
