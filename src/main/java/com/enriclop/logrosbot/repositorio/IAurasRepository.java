package com.enriclop.logrosbot.repositorio;

import com.enriclop.logrosbot.modelo.Aura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAurasRepository extends JpaRepository<Aura, Integer> {


}
