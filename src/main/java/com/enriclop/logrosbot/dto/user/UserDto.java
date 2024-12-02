package com.enriclop.logrosbot.dto.user;

import com.enriclop.logrosbot.modelo.Aura;
import com.enriclop.logrosbot.modelo.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDto {

    private int id;

    private String twitchId;

    private String username;

    private Integer score;

    private String avatar;

    private int achievements;

    private List<Aura> auras;

    UserDto(User user) {
        this.id = user.getId();
        this.twitchId = user.getTwitchId();
        this.username = user.getUsername();
        this.score = user.getScore();
        this.avatar = user.getAvatar();
        this.achievements = user.getAchievements().size();
        this.auras = user.getAuras();
    }

    public static UserDto fromUser(User user) {
        return new UserDto(user);
    }

    public static List<UserDto> fromUsers(List<User> users) {
        return users.stream().map(UserDto::new).toList();
    }
}
