package com.enriclop.logrosbot.security;

import com.enriclop.logrosbot.dto.user.AdminUser;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "settings")
public class Settings {

    private String channelName;
    private String tokenChannel;
    private String botUsername;
    private String tokenBot;
    private String domain;
    private String adminUsername;
    private String adminPassword;
    private AdminUser adminUser;
    private String clientId;
    private String steamKey;
    private String steamId;
    private String xboxKey;

    @PostConstruct
    public void initAdminUser() {
        this.adminUser = new AdminUser(adminUsername, adminPassword);

        log.info("Admin user: " + adminUsername + " Password: " + adminPassword);
    }

    public String getoAuthTokenChannel() {
        return "oauth:" + tokenChannel;
    }

    public String getoAuthTokenBot() {
        return "oauth:" + tokenBot;
    }
}
