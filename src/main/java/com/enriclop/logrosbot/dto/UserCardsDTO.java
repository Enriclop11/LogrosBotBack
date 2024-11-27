package com.enriclop.logrosbot.dto;

import com.enriclop.logrosbot.modelo.Badge;
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

    private List<Badge> badges;

    public UserCardsDTO(User user) {
        this.id = user.getId();
        this.twitchId = user.getTwitchId();
        this.username = user.getUsername();
        this.score = user.getScore();
        this.avatar = user.getAvatar();
        this.achievements = AchievementDTO.fromPhotoCards(user.getAchievements());
        this.badges = user.getBadges();
    }

    public static List<UserCardsDTO> fromUsers(List<User> users) {
        return users.stream().map(UserCardsDTO::new).toList();
    }
}
