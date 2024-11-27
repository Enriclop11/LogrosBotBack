package com.enriclop.logrosbot.repositorio;

import com.enriclop.logrosbot.modelo.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAchievementRepository extends JpaRepository<Achievement, Integer> {

}
