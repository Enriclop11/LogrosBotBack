package com.enriclop.logrosbot.controller;

import com.enriclop.logrosbot.dto.aura.AuraDTO;
import com.enriclop.logrosbot.modelo.Aura;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.servicio.AurasService;
import com.enriclop.logrosbot.servicio.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class MarketplaceController {

    @Autowired
    private UserService userService;

    @Autowired
    private AurasService aurasService;

    @GetMapping("/marketplace")
    public List<AuraDTO> getMarketplace() {
        return aurasService.getShopAuras();
    }


    @PostMapping("/marketplace")
    public List<AuraDTO> getMarketplace(@RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);

        List<AuraDTO> aurasList = aurasService.getShopAuras();

        aurasList.forEach(aura -> {
            aura.canBuy = user.getScore() >= aura.price;
            if (aura.canBuy) aura.canBuy = user.getAchievements().size() >= aura.requirements.numAchievements;
            if (aura.canBuy) aura.canBuy = user.getAuras().stream().noneMatch(a -> a.getAuraId() == aura.id);
        });

        return aurasList;
    }

    @PostMapping("/marketplace/buy")
    public void buyAura(@RequestParam AuraBuyDTO auraBuyDTO, @RequestHeader("Authorization") String token) {
        User buyer = userService.getUserByToken(token);
        AuraDTO aura = aurasService.getShopAuraById(auraBuyDTO.auraId);

        if (buyer.getScore() < aura.price) {
            throw new RuntimeException("Not enough score");
        }

        if (buyer.getAchievements().size() < aura.requirements.numAchievements) {
            throw new RuntimeException("Not enough achievements");
        }

        if (buyer.getAuras().stream().anyMatch(a -> a.getAuraId() == aura.id)) {
            throw new RuntimeException("Already bought");
        }

        buyer.setScore(buyer.getScore() - aura.price);
        buyer.getAuras().add(new Aura(aura));
        userService.saveUser(buyer);
    }

    static class AuraBuyDTO {
        public int auraId;
    }

}
