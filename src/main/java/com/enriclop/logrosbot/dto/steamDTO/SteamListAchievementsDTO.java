package com.enriclop.logrosbot.dto.steamDTO;

import java.util.List;

public class SteamListAchievementsDTO {
    public String steamID;
    public String gameName;
    public List<SteamAchievementDTO> achievements;
    public boolean success;
}
