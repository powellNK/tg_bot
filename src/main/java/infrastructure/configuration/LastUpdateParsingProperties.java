package infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class LastUpdateParsingProperties {
    private static final String FILE_PATH = "properties";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = LoggerFactory.getLogger(LastUpdateParsingProperties.class);
    static {
        // Создать файл конфигурации при загрузке класса, если он не существует
        createFileIfNotExists();
    }

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
            if (dateTimeString != null) {
                LocalDateTime lastUpdate = LocalDateTime.parse(dateTimeString, formatter);
                logger.info("Дата последнего обновления загружена: {}", lastUpdate);
                return lastUpdate;
            } else {
                logger.warn("Ключ 'lastUpdate' не найден в файле конфигурации");
                return null;
            }
        } catch (IOException e) {
            logger.error("Ошибка при загрузке файла: {}", FILE_PATH, e);
            return null;
        }
    }

    private static void createFileIfNotExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try (OutputStream output = new FileOutputStream(file)) {
                Properties properties = new Properties();
                properties.setProperty("lastUpdate", null);
                properties.store(output, "Initial configuration");
                logger.info("Файл конфигурации был создан: {}", FILE_PATH);
            } catch (IOException e) {
                logger.error("Ошибка при создании файла конфигурации", e);
            }
        }
    }
}

