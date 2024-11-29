package com.enriclop.logrosbot.dto.xboxDTO;

import java.util.List;

public class XboxAchievementDTO {
    public String id;
    public String serviceConfigId;
    public String name;
    public String progressState;
    public List<XboxMediaAssetDTO> mediaAssets;
    public List<String> platforms;
    public boolean isSecret;
    public String description;
    public String lockedDescription;
    public String productId;
    public String achievementType;
    public String participationType;
    public String timeWindow;
    public String estimatedTime;
    public String deeplink;
    public boolean isRevoked;
    public XboxRarityDTO rarity;
}
