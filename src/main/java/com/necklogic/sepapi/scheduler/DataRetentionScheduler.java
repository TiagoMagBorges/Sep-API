package com.necklogic.sepapi.scheduler;

import com.necklogic.sepapi.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataRetentionScheduler {

    private final ProfessorRepository professorRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanUpOldData() {
        log.info("Iniciando rotina de Hard Delete para retenção de dados.");

        LocalDateTime thresholdDate = LocalDateTime.now().minusYears(2);

        try {
            professorRepository.hardDeleteOldProfessors(thresholdDate);
            log.info("Rotina de limpeza de dados finalizada com sucesso.");

        } catch (Exception e) {
            log.error("Erro durante a execução do Hard Delete: ", e);
        }
    }
}