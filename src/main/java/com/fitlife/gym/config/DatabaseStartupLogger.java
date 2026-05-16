package com.fitlife.gym.config;

import com.fitlife.gym.domain.repository.MemberRepository;
import com.fitlife.gym.domain.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseStartupLogger implements ApplicationRunner {

    private final DataSource dataSource;
    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            String catalog = connection.getCatalog();
            log.info("MySQL connected: catalog={}, url={}", catalog, url);
        }

        long orgCount = organizationRepository.count();
        long memberCount = memberRepository.count();
        log.info("Database ready — organizations: {}, members: {}", orgCount, memberCount);

        if (orgCount == 0) {
            log.warn("No seed data found. Run scripts/setup-database.ps1 or enable Flyway migrations.");
        }
    }
}
