package ru.job4j.tracker;

import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

public class SqlTrackerTest {

    private static final String TEST_NAME = "Test1";
    private static final LocalDateTime TEST_CREATED = LocalDateTime.now().withNano(0);

    private static Connection connection;

    private static SqlTracker tracker;

    @BeforeAll
    public static void initConnection() {
        try (InputStream in = SqlTracker.class.getClassLoader().getResourceAsStream("db/liquibase_test.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));
            tracker = new SqlTracker(connection);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @AfterEach
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("delete from items;")) {
            statement.execute();
        }
    }

    private Item addTestItem() {
        Item item = new Item(TEST_NAME, TEST_CREATED);
        item.setId(tracker.add(item).getId());
        return item;
    }

    @Test
    public void whenSaveItemAndFindByGeneratedIdThenMustBeTheSame() {
        Item item = addTestItem();
        assertThat(tracker.findById(item.getId())).isEqualTo(item);
    }

    @Test
    void whenFindByIdNotExistsThenNotFound() {
        Item item = addTestItem();
        assertThat(tracker.findById(item.getId() + 1)).isNull();
    }

    @Test
    void whenReplaceThenReplaced() {
        Item item1 = addTestItem();
        Item item2 = new Item(item1.getId(), TEST_NAME + "2", LocalDateTime.now().withNano(0));
        tracker.replace(item1.getId(), item2);
        assertThat(tracker.findById(item1.getId())).isEqualTo(item2);
    }

    @Test
    void whenDeleteThenDeleted() {
        Item item = addTestItem();
        tracker.delete(item.getId());
        assertThat(tracker.findById(item.getId())).isNull();
    }
}