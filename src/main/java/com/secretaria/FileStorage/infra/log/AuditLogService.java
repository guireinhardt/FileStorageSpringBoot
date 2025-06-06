package com.secretaria.FileStorage.infra.log;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuditLogService {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_PATH = LOG_DIR + "/audit-" + LocalDate.now() + ".txt";


    public AuditLogService() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs(); // Cria a pasta logs se não existir
        }
    }

    public void log(String username, String action, String detail) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            String timestamp = LocalDateTime.now().toString();
            String logEntry = String.format("[%s] Usuário: %s | Ação: %s | Detalhes: %s",
                    timestamp, username, action, detail);
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao escrever log: " + e.getMessage());
        }
    }
}
