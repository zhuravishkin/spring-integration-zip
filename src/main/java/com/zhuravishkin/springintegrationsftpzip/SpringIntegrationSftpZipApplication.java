package com.zhuravishkin.springintegrationsftpzip;

import com.zhuravishkin.springintegrationsftpzip.service.SftpService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringIntegrationSftpZipApplication implements CommandLineRunner {
    private final SftpService sftpService;

    public SpringIntegrationSftpZipApplication(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringIntegrationSftpZipApplication.class, args);
    }

    @Override
    public void run(String... args) {
        sftpService.writeLiensToSftpFile();
    }
}
