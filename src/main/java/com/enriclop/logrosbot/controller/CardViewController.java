package com.enriclop.logrosbot.controller;

import com.enriclop.logrosbot.servicio.AchievementService;
import com.enriclop.logrosbot.servicio.UserService;
import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CardViewController {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserService userService;

    @Autowired
    TwitchConnection twitchConnection;

    @GetMapping ("/photoSpawn")
    public String pokemonSpawn() {
        return "photocards/spawn";
    }
    @GetMapping("/battle")
    public String battle() {
        return "photocards/combat";
    }
}
