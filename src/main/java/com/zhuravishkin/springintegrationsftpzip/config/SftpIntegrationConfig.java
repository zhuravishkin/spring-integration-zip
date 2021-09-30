package com.zhuravishkin.springintegrationsftpzip.config;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.filters.AcceptAllFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.filters.SftpRegexPatternFileListFilter;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.integration.transformer.StreamTransformer;

@Slf4j
@EnableIntegration
@Configuration
public class SftpIntegrationConfig {
    private final String sftpHost;
    private final String sftpUser;
    private final String sftpPassword;
    private final String sftPath;
    private final int sftPort;

    public SftpIntegrationConfig(@Value("${sftp.host}") String sftpHost,
                                 @Value("${sftp.user}") String sftpUser,
                                 @Value("${sftp.password}") String sftpPassword,
                                 @Value("${sftp.path}") String sftPath,
                                 @Value("${sftp.port}") int sftPort) {
        this.sftpHost = sftpHost;
        this.sftpUser = sftpUser;
        this.sftpPassword = sftpPassword;
        this.sftPath = sftPath;
        this.sftPort = sftPort;
    }

    @Bean
    public SessionFactory<LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory(true);
        sessionFactory.setHost(sftpHost);
        sessionFactory.setPort(sftPort);
        sessionFactory.setUser(sftpUser);
        sessionFactory.setPassword(sftpPassword);
        sessionFactory.setAllowUnknownKeys(true);

        return new CachingSessionFactory<>(sessionFactory);
    }

    @Bean
    public SftpRemoteFileTemplate template() {
        return new SftpRemoteFileTemplate(sftpSessionFactory());
    }

    @Bean
    public ExpressionEvaluatingRequestHandlerAdvice after() {
        ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
        advice.setOnSuccessExpressionString("@template.remove(headers['file_remoteDirectory'] + '/' + headers['file_remoteFile'])");
        advice.setPropagateEvaluationFailures(true);

        return advice;
    }

    @Bean
    public IntegrationFlow sftpInboundFlow() {
        CompositeFileListFilter<LsEntry> compositeFileListFilter = new CompositeFileListFilter<>();
        compositeFileListFilter.addFilter(new SftpRegexPatternFileListFilter(".*\\.csv$"));
        compositeFileListFilter.addFilter(new AcceptAllFileListFilter<>());

        return IntegrationFlows
                .from(Sftp.inboundStreamingAdapter(template())
                                .remoteDirectory(sftPath)
                                .maxFetchSize(1)
                                .filter(compositeFileListFilter)
                        , c -> c.poller(Pollers.fixedDelay(1000)
                                .errorHandler(throwable -> log.error("Error: " + throwable.getMessage()))))
                .transform(new StreamTransformer("UTF-8"))
                .handle("connector", "handleMessage", c -> c.advice(after()))
                .get();
    }
}
