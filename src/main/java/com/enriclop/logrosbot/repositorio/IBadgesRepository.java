package com.enriclop.logrosbot.repositorio;

import com.enriclop.logrosbot.modelo.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBadgesRepository extends JpaRepository<Badge, Integer> {


}
