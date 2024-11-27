package com.enriclop.logrosbot.dto;

import com.enriclop.logrosbot.modelo.Badge;
import com.enriclop.logrosbot.modelo.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserProfileDto {

    private int id;

    private String twitchId;

    private String username;

    private Integer score;

    private String avatar;

    private int achievements;

    private List<Badge> badges;

    UserProfileDto(User user) {
        this.id = user.getId();
        this.twitchId = user.getTwitchId();
        this.username = user.getUsername();
        this.score = user.getScore();
        this.avatar = user.getAvatar();
        this.achievements = user.getAchievements().size();
        this.badges = user.getBadges();
    }

    public static UserProfileDto fromUser(User user) {
        return new UserProfileDto(user);
    }

    public static List<UserProfileDto> fromUsers(List<User> users) {
        return users.stream().map(UserProfileDto::new).toList();
    }
}
