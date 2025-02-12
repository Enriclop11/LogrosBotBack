package com.enriclop.logrosbot.enums;

import lombok.Getter;

@Getter
public enum Rarity {

    COMMON(1),
    UNCOMMON(2),
    RARE(3),
    EPIC(4),
    LEGENDARY(5),
    MYTHIC(6);

    private final int value;

    Rarity(int value) {
        this.value = value;
    }

}
