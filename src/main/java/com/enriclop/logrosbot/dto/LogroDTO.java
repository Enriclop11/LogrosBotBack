package com.enriclop.logrosbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LogroDTO {
    @JsonProperty("ID")
    private String id;
    private String name;
    private String description;
    private String photo;
    private int rarity;

    public LogroDTO() {
    }
}
