package com.enriclop.logrosbot.repositorio;

import com.enriclop.logrosbot.modelo.Marketplace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMarketplaceRepository extends JpaRepository<Marketplace, Integer> {
}
