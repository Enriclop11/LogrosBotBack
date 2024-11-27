package com.enriclop.logrosbot.controller;

import com.enriclop.logrosbot.modelo.Marketplace;
import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.servicio.AchievementService;
import com.enriclop.logrosbot.servicio.MarketplaceService;
import com.enriclop.logrosbot.servicio.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class MarketplaceController {

    @Autowired
    private MarketplaceService marketplaceService;

    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;

    @GetMapping("/marketplace")
    public List<Marketplace> getMarketplace() {
        return marketplaceService.getMarketplace();
    }

    @PostMapping("/marketplace/buy")
    public void buyCard(@RequestParam int cardId, @RequestHeader("Authorization") String token) {
        User buyer = userService.getUserByToken(token);
        Marketplace market = marketplaceService.getMarketplaceById(cardId);
        Achievement card = market.getCard();
        int price = market.getPrice();
        User seller = card.getUser();

        if (buyer.getScore() >= price) {
            buyer.minusScore(price);
            userService.saveUser(buyer);
            seller.addScore(price);
            userService.saveUser(seller);

            achievementService.changeUser(card, buyer);
        }
    }

    @PostMapping("/marketplace/offer")
    public void sellCard(@RequestParam SellDTO sellDTO, @RequestHeader("Authorization") String token) {

        User seller = userService.getUserByToken(token);
        Achievement card = achievementService.getAchievementById(sellDTO.cardId);

        if (card.getUser().getId() != seller.getId()) {
            return;
        }

        int price = sellDTO.price;

        Marketplace market = new Marketplace();
        market.setCard(card);
        market.setPrice(price);

        marketplaceService.saveMarketplace(market);
    }

    public static class SellDTO {
        private int cardId;
        private int price;
    }

}
