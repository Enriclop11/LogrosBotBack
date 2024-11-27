package com.enriclop.logrosbot.repositorio;

import com.enriclop.logrosbot.modelo.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IItemsRepository extends JpaRepository<Items, Integer> {
}
