package com.enriclop.logrosbot.controller;

import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.servicio.AchievementService;
import com.enriclop.logrosbot.servicio.UserService;
import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class CardController {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserService userService;

    @Autowired
    private TwitchConnection twitchConnection;

    @GetMapping("/achievements")
    public List<Achievement> getPokemons() {
        return achievementService.getAchievements();
    }

    @GetMapping("/achievements/{id}")
    public Achievement getPokemonById(Integer id) {
        return achievementService.getAchievementById(id);
    }

    @GetMapping("/achievements/wild/sprite")
    public String getWildPokemonSprite() {
        if (twitchConnection.getWildAchievement() != null)
            return twitchConnection.getWildAchievement().getPhoto();
        else
            return null;
    }

    @DeleteMapping("/achievements/{id}")
    public void deletePokemon(@PathVariable Integer id, @RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);
        Achievement achievement = achievementService.getAchievementById(id);

        if (user != null && achievement != null && user.getAchievements().contains(achievement)) {
            user.getAchievements().remove(achievement);
            userService.saveUser(user);
            achievementService.deleteAchievement(id);
            log.info("User " + user.getUsername() + " deleted card " + achievement.getName());
        } else {
            log.warn("User " + user.getUsername() + " attempted to delete a card that does not exist or does not belong to them.");
        }
    }
}