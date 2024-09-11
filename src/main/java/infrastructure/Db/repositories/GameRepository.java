package infrastructure.Db.repositories;

import domain.Game;

import java.sql.Connection;

public class GameRepository {
    private final Connection connection;
    public GameRepository(Connection connection) {
        this.connection = connection;
    }

    public void createGame(Game game) {
    }
}
