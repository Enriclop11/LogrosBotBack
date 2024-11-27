package com.enriclop.logrosbot.websockets.card;

import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.servicio.AchievementService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CardInfoController {

    @Autowired
    private AchievementService achievementService;

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting(String message) throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("content", "Hello, " + message + "!");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(data);
    }

    @MessageMapping("/pokemon/wild")
    @SendTo("/topic/pokemon/wild")
    public Achievement sendWildPokemon(Achievement wildPokemon) {
        return wildPokemon;
    }

    @MessageMapping("/pokemon/catch")
    @SendTo("/topic/pokemon/catch")
    public String catchPokemon(String pokeball, Boolean caught) {
        Map<String, String> data = new HashMap<>();
        data.put("pokeball", pokeball);
        data.put("caught", caught.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
