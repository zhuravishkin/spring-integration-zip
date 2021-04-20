package com.zhuravishkin.springintegrationsftpzip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
        try (PipedInputStream pipedInputStream = new PipedInputStream()) {
            try (PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream)) {
                for (String s : strings) {
                    pipedOutputStream.write((s + "\n").getBytes(StandardCharsets.UTF_8));
                }
                pipedOutputStream.flush();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            String path = "/2021/04/20";
            sftpRemoteFileTemplate.setRemoteDirectoryExpression(new LiteralExpression(path));
            sftpRemoteFileTemplate.execute(session -> {
                StringBuilder builder = new StringBuilder(".sftp");
                Arrays.stream(path.split("/")).forEach(s -> {
                    try {
                        session.mkdir(builder.append("/").append(s).toString());
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                });
                session.write(pipedInputStream, ".sftp/2021/04/20/test.log");
                return null;
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
