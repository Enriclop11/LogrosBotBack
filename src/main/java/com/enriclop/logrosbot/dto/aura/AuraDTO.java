package com.enriclop.logrosbot.dto.aura;

import com.enriclop.logrosbot.enums.Rarity;

public class AuraDTO {
    public int id;
    public String name;
    public String description;
    public String image;
    public int numAchievements;
    public Rarity rarity;
    public int price;
    public AuraRequirementsDTO requirements;
    public boolean canBuy;
}
