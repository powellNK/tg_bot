package services;

import domain.Player;
import infrastructure.Db.repositories.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlayerService {
    private final PlayerRepository playerRepository;
    private final TeamService teamService;
    private final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(PlayerRepository playerRepository, TeamService teamService) {
        this.playerRepository = playerRepository;
        this.teamService = teamService;
    }

    public void createPlayer(String fio, int age, int gameNumber, int height, int teamId, String role, int season) {
        if (!isPlayerExists(fio)) {
            playerRepository.createPlayer(Player.builder().fio(fio).age(age).gameNumber(gameNumber).height(height).role(role).build());
            logger.info("Игрок добавлен");
        }
        if (isTeamExistsInSeason(teamId, season)) {
            playerRepository.addNewSeasonPlayer(Player.builder().fio(fio).team_id((short) teamId).season(season).build());
            logger.info("Информация об игроке обновлена");
        }
    }

    private boolean isTeamExistsInSeason(int teamId, int season) {
        return teamService.isTeamExistsInSeason(teamId, season);
    }

    private boolean isPlayerExists(String fio) {
        return playerRepository.isPlayerExists(Player.builder().fio(fio).build());
    }

    public List<Player> getPlayers(int season, short teamId) {
        return playerRepository.getPlayers(season, teamId);
    }
}


