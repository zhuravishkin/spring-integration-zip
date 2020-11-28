package com.zhuravishkin.springintegrationsftpzip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.ChainFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.integration.zip.splitter.UnZipResultSplitter;
import org.springframework.integration.zip.transformer.UnZipTransformer;
import org.springframework.integration.zip.transformer.ZipResultType;

import java.io.File;
import java.util.concurrent.TimeUnit;

@EnableIntegration
@Configuration
public class SftpIntegrationConfig {
    @Bean
    public IntegrationFlow thirdpatysystemAgentDemographicFlow() {
        return IntegrationFlows
                .from(inputFileSource(), spec -> spec.poller(Pollers.fixedDelay(1000, TimeUnit.MILLISECONDS)))
                .transform(unZipTransformer())
                .handle(new FileToStringTransformer())
                .handle(message -> System.out.println(message.getPayload()))
                .get();
    }

    @Bean
    public MessageSource<File> inputFileSource() {
        FileReadingMessageSource src = new FileReadingMessageSource();
        src.setDirectory(new File("C:/Users/User/.sftp"));
        src.setAutoCreateDirectory(true);

        ChainFileListFilter<File> chainFileListFilter = new ChainFileListFilter<>();
        chainFileListFilter.addFilter(new AcceptOnceFileListFilter<>());
        chainFileListFilter.addFilter(new RegexPatternFileListFilter("(?i)^.+\\.zip$"));
        src.setFilter(chainFileListFilter);
        return src;
    }

    @Bean
    public UnZipTransformer unZipTransformer() {
        UnZipTransformer unZipTransformer = new UnZipTransformer();
        unZipTransformer.setExpectSingleResult(true);
        unZipTransformer.setZipResultType(ZipResultType.FILE);
        unZipTransformer.setWorkDirectory(new File("C:/Users/User/tmp"));
        unZipTransformer.setDeleteFiles(true);
        return unZipTransformer;
    }

    @Bean
    public UnZipResultSplitter splitter() {
        return new UnZipResultSplitter();
    }
}
