package com.delivery.service.integration.testcontainers

import mu.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent
import org.testcontainers.containers.PostgreSQLContainer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.listDirectoryEntries

class PostgresTestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        assertThatDbScriptExists()

        logger.info("Starting Postgres container...")
        postgresContainer.start()
        logger.info("Started Postgres container on ${postgresContainer.jdbcUrl}")

        TestPropertyValues.of(
            "spring.datasource.url=${postgresContainer.jdbcUrl}",
            "spring.datasource.password=${postgresContainer.password}",
            "spring.datasource.username=${postgresContainer.username}"
        ).applyTo(applicationContext.environment)

        applicationContext.addApplicationListener {
            if (it is ContextClosedEvent) {
                logger.info("Stopping Postgres container...")
                postgresContainer.stop()
            }
        }
    }

    // Flyway Migration
    companion object {
        val logger = KotlinLogging.logger {}

        private const val PATH_TO_SCHEMA = "src/test/resources/db.migration"

        val postgresContainer = PostgreSQLContainer("postgres:17")
            .apply {
                withDatabaseName("postgres")
                withUsername("postgres")
                withPassword("postgres")
            }
    }

    private fun assertThatDbScriptExists() {
        val path: Path =
            Paths.get(PATH_TO_SCHEMA)
        if (Files.isDirectory(path)) {
            if (path.listDirectoryEntries("*.sql").isEmpty()) {
                throw RuntimeException("Migrations not found. Please check the path: $path")
            }
        } else {
            throw RuntimeException("Migration folder not found")
        }
    }
}