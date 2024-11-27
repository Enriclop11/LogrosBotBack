package com.enriclop.logrosbot.modelo;

import com.enriclop.logrosbot.dto.LogroDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "achievements")
@Data
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String achievementId;

    private String name;

    private String description;

    private String photo;

    private double rarity;

    private String gameName;

    private String gameId;

    private String platform;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public Achievement() {
    }

    public Achievement(Achievement achievement) {
        this.name = achievement.getName();
        this.description = achievement.getDescription();
        this.photo = achievement.getPhoto();
        this.rarity = achievement.getRarity();
    }

    public Achievement(LogroDTO logroDTO, String photo) {
        this.name = logroDTO.getName();
        this.achievementId = logroDTO.getId();
        this.description = logroDTO.getDescription();
        this.photo = photo;
        this.rarity = logroDTO.getRarity();
    }

    public Achievement(String name, String description, String photo, int rarity) {
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.rarity = rarity;
    }

    public Achievement(String name, String description, String photo, int rarity, User user) {
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.rarity = rarity;
        this.user = user;
    }

    @Override
    public String toString() {
        return "Logro{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", photo='" + photo + '\'' +
                ", rarity=" + rarity +
                '}';
    }
}
