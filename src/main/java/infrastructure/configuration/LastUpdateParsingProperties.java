package infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class LastUpdateParsingProperties {
    private static final String FILE_PATH = "infrastructure.configuration.properties";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = LoggerFactory.getLogger(LastUpdateParsingProperties.class);

    public static void saveLastUpdate(LocalDateTime dateTime) {
        Properties properties = new Properties();
        try (OutputStream output = new FileOutputStream(FILE_PATH)) {
            properties.setProperty("lastUpdate", dateTime.format(formatter));
            properties.store(output, null);
            logger.info("Дата последнего обновления сохранена успешно: {}", dateTime);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении даты последнего обновления", e);
        }
    }

    public static LocalDateTime loadLastUpdate() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(FILE_PATH)) {
            properties.load(input);
            String dateTimeString = properties.getProperty("lastUpdate");
            LocalDateTime lastUpdate = LocalDateTime.parse(dateTimeString, formatter);
            logger.info("Дата последнего обновления загружена: {}", lastUpdate);
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (IOException | NullPointerException e) {
            logger.warn("Ошибка при загрузке даты последнего обновления, возможно файл не существует", e);
            return null;
        }
    }
}

