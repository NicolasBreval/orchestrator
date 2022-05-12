package org.nitb.orchestrator.test.logging

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import org.slf4j.event.Level
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.io.path.name
import org.junit.jupiter.api.Assertions.*

class LoggingTest {

    @ParameterizedTest
    @MethodSource("testParameters")
    fun checkLoggingFileAppender(loggerName: String, level: String, maxFileSize: String, message: String, loops: Int, expectedFiles: Int) {
        ConfigManager.setProperties(mapOf(
            ConfigNames.LOGGING_LEVEL to level,
            ConfigNames.LOGGING_FOLDER to "test-logs",
            ConfigNames.LOGGING_DATE_PATTERN to "yyyy-MM-dd",
            ConfigNames.LOGGING_MAX_FILE_SIZE to maxFileSize
        ))

        val logger = LoggingManager.getLogger(loggerName)

        for (i in 0 until loops) {
            logger.log(Level.valueOf(level), message)
        }

        assertTrue(Files.exists(Paths.get("test-logs")))
        assertEquals(Files.walk(Paths.get("test-logs")).filter { it.name.startsWith(loggerName) }.count().toInt(), expectedFiles)
    }

    companion object {
        @JvmStatic
        private fun testParameters(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("test-1-logger", "INFO", "1kb", "A", 1, 1),
                Arguments.of("test-2-logger", "INFO", "1kb", "A", 100, 3)
            )
        }

        @JvmStatic
        @BeforeAll
        private fun clean() {
            if (Files.exists(Paths.get("test-logs"))) {
                Files.walk(Paths.get("test-logs")).filter(Files::isRegularFile).forEach {
                    Files.delete(it)
                }
                Files.delete(Paths.get("test-logs"))
            }
        }
    }

}