package services;

import domain.Player;
import infrastructure.Db.repositories.PlayerRepository;

public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void createPlayer(String fio, int age, int gameNumber, int height, short teamId, String role, int season) {
        if (!isPlayerExists(fio)) {
            playerRepository.createPlayer(Player.builder().fio(fio).age(age).gameNumber(gameNumber).height(height).role(role).build());
        }
        playerRepository.addNewSeasonPlayer(fio, teamId, season);
    }

    private boolean isPlayerExists(String fio) {
        return playerRepository.isPlayerExists(Player.builder().fio(fio).build());
    }
}
