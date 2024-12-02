package com.enriclop.logrosbot.servicio;

import com.enriclop.logrosbot.dto.aura.AuraDTO;
import com.enriclop.logrosbot.dto.aura.AuraListDTO;
import com.enriclop.logrosbot.modelo.Aura;
import com.enriclop.logrosbot.repositorio.IAurasRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static discord4j.core.event.EventDispatcher.log;

@Service
public class AurasService {

    @Autowired
    private IAurasRepository aurasRepository;

    public AurasService(IAurasRepository aurasRepository) {
        this.aurasRepository = aurasRepository;
    }

    public List<Aura> getAuras() {
        return aurasRepository.findAll();
    }

    public void saveAura(Aura aura) {
        aurasRepository.save(aura);
    }

    public Aura getAuraById(Integer id) {
        return aurasRepository.findById(id).orElse(null);
    }

    public void deleteAura(Integer id) {
        aurasRepository.deleteById(id);
    }

    public List<AuraDTO> getShopAuras() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/achievementsData/auras.json")) {
            if (is == null) {
                throw new IOException("File not found: auras");
            }
            AuraListDTO auras = mapper.readValue(is, AuraListDTO.class);

            return auras.auras;
        } catch (Exception e) {
            log.error("Error reading marketplace file", e);
        }
        return null;
    }

    public AuraDTO getShopAuraById(int id) {
        return getShopAuras().stream().filter(aura -> aura.id == id).findFirst().orElse(null);
    }

}
