package com.enriclop.logrosbot.modelo;


import com.enriclop.logrosbot.dto.aura.AuraDTO;
import com.enriclop.logrosbot.enums.Rarity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "auras")
@Data
public class Aura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private int auraId;

    private String name;

    private String description;

    private String image;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    public Aura() {
    }

    public Aura(String name, String description, String image, Rarity rarity) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.rarity = rarity;
    }

    public Aura(AuraDTO auraDTO) {
        this.auraId = auraDTO.id;
        this.name = auraDTO.name;
        this.description = auraDTO.description;
        this.image = auraDTO.image;
        this.rarity = auraDTO.rarity;
    }

}
