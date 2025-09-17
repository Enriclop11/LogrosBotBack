package com.enriclop.logrosbot.dto.user;

import com.enriclop.logrosbot.modelo.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {

    private int id;

    private String twitchId;

    private String username;

    private Integer score;

    private String avatar;

    private int achievements;

    UserDto(User user) {
        this.id = user.getId();
        this.twitchId = user.getTwitchId();
        this.username = user.getUsername();
        this.score = user.getScore();
        this.avatar = user.getAvatar();
        this.achievements = user.getAchievements().size();
    }

    public UserDto(Integer id, String twitchId, String username, Integer score, String avatar, Long achievements) {
        this.id = id;
        this.twitchId = twitchId;
        this.username = username;
        this.score = score;
        this.avatar = avatar;
        this.achievements = achievements.intValue();
    }

    public static UserDto fromUser(User user) {
        return new UserDto(user);
    }

    public static List<UserDto> fromUsers(List<User> users) {
        return users.stream().map(UserDto::new).toList();
    }
}
