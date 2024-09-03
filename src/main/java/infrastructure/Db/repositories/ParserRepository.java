package infrastructure.Db.repositories;

import java.sql.Connection;

public class ParserRepository {
    private final Connection connection;
    public ParserRepository(Connection connection) {
        this.connection = connection;
    }
}
