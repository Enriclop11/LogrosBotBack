package com.enriclop.logrosbot.controller;

import com.enriclop.logrosbot.dto.login.LoginRequest;
import com.enriclop.logrosbot.dto.login.LoginResponse;
import com.enriclop.logrosbot.dto.user.UserCardsDTO;
import com.enriclop.logrosbot.dto.user.UserDto;
import com.enriclop.logrosbot.dto.user.UserProfileDto;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.security.Settings;
import com.enriclop.logrosbot.servicio.UserService;
import com.enriclop.logrosbot.twitchConnection.TwitchConnection;
import com.enriclop.logrosbot.utilities.Utilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TwitchConnection twitchConnection;

    @Autowired
    private Settings settings;

    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public UserCardsDTO getUserByIdOrUsername(@PathVariable String id) {
        try{
            Integer idInt = Integer.parseInt(id);
            User user = userService.getUserById(idInt);
            if (user == null) {
                user = userService.getUserByUsername(id);
            }
            return new UserCardsDTO(user);
        } catch (NumberFormatException e) {
            User user = userService.getUserByUsername(id);
            return new UserCardsDTO(user);
        }
    }

    @GetMapping("/streamUsers")
    public List<String> getStreamUsers() {
        return twitchConnection.getSetWatchTime().usersInThisStream.stream().map(User::getUsername).toList();
    }

    @PostMapping("/token")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login request: " + loginRequest);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            String token = userService.generateToken(authentication);
            return new LoginResponse(token);
        } catch (AuthenticationException e) {
            log.error("Error in login", e);
            return null;
        }
    }

    @PostMapping("/myuser")
    public UserProfileDto getMyUser(@RequestHeader("Authorization") String token) {
        User user = userService.getUserByToken(token);

        log.info("Getting user profile for: " + user.getUsername());

        boolean isModerator = settings.getModeratorUsers()
                .stream()
                .anyMatch(moderator -> moderator.getUsername().equals(user.getUsername()));

        log.info("Is moderator: " + isModerator);

        return UserProfileDto.fromUser(user, isModerator);
    }

    @PostMapping("/changePassword")
    public void changePassword(@RequestHeader("Authorization") String token, @RequestBody PasswordDTO passwordDTO) {
        User userToken = userService.getUserByToken(token);
        log.info("Changing password for user: " + userToken.getUsername());
        userToken.setPassword(passwordDTO.password);
        userService.saveUser(userToken);
    }
    
    static class PasswordDTO {
        public String password;
    }

    static class SelectCardDTO {
        public Integer cardId;
    }

    @PostMapping("/loginTwitch")
    public LoginResponse loginTwitch(@RequestBody TwitchLoginDTO twitchLoginDTO) {
        String url = "https://api.twitch.tv/helix/users";

        org.springframework.http.HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + twitchLoginDTO.code);
        headers.set("Client-ID", twitchConnection.getSettings().getClientId());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        log.info("Response: " + response.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");
            if (data.isArray() && data.size() > 0) {
                JsonNode user = data.get(0);

                return setPassword(user.get("id").asText());
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON response", e);
        }

        return null;
    }

    public LoginResponse setPassword(String id)  {
        User user = userService.getUserByTwitchId(id);
        if (user == null) {
            twitchConnection.start(id);
            user = userService.getUserByTwitchId(id);
        }

        String password = Utilities.generatePassword();
        user.setPassword(password);
        userService.saveUser(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), password)
        );

        String token = userService.generateToken(authentication);
        return new LoginResponse(token);
    }


    static class TwitchLoginDTO {
        public String code;
    }

    /*
    @PostMapping("/selectAchievement")
    public void selectAchievement(@RequestHeader("Authorization") String token, @RequestBody SelectAchievement achievement) {
        User user = userService.getUserByToken(token);
        user.setSelectedAchievement(achievement.achievementId);
        userService.saveUser(user);
    }

    static class SelectAchievement {
        public Integer achievementId;
    }

     */

}
