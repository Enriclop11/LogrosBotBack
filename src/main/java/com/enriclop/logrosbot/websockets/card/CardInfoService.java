package com.enriclop.logrosbot.websockets.card;

import com.enriclop.logrosbot.modelo.Achievement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CardInfoService {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendWildCard(Achievement wildPokemon) {
        template.convertAndSend("/topic/pokemon/wild", wildPokemon);
    }

    public void sendCatchPokemon(String pokeball, Boolean caught) {
        Map<String, String> data = new HashMap<>();
        data.put("pokeball", pokeball.toLowerCase());
        data.put("caught", caught.toString());
        template.convertAndSend("/topic/pokemon/catch", data);
    }

    public void endCombat() {
        Map<String, Object> data = new HashMap<>();
        data.put("end", true);
        template.convertAndSend("/topic/card/combat", data);
    }
}
