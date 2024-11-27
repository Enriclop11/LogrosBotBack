package com.enriclop.logrosbot.servicio;

import com.enriclop.logrosbot.achievementApi.SteamConnection;
import com.enriclop.logrosbot.modelo.Achievement;
import com.enriclop.logrosbot.modelo.User;
import com.enriclop.logrosbot.repositorio.IAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AchievementService {

    @Autowired
    private IAchievementRepository achievementRepository;

    @Autowired
    private SteamConnection steamConnection;

    public AchievementService(IAchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    public List<Achievement> getAchievements() {
        return achievementRepository.findAll();
    }

    public void saveAchievement(Achievement achievement) {
        achievementRepository.save(achievement);
    }

    public Achievement getAchievementById(Integer id) {
        return achievementRepository.findById(id).orElse(null);
    }

    public void deleteAchievement(Integer id) {
        achievementRepository.deleteById(id);
    }

    public void changeUser(Achievement achievement, User user) {
        achievement.setUser(user);
        achievementRepository.save(achievement);
    }
}
