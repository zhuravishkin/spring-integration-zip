package com.zhuravishkin.springintegrationsftpzip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class SftpService {
    private final SftpRemoteFileTemplate sftpRemoteFileTemplate;

    public SftpService(SftpRemoteFileTemplate sftpRemoteFileTemplate) {
        this.sftpRemoteFileTemplate = sftpRemoteFileTemplate;
    }

    public void writeLiensToSftpFile() {
        List<String> strings = List.of("alpha", "beta", "gamma");
        PipedInputStream pipedInputStream = new PipedInputStream();
        try (PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream)) {
            for (String s : strings) {
                pipedOutputStream.write((s + "\n").getBytes(StandardCharsets.UTF_8));
            }
            pipedOutputStream.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        sftpRemoteFileTemplate.execute(session -> {
            session.write(pipedInputStream, ".sftp/test.log");
            return null;
        });
    }
}
