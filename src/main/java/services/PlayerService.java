package services;

import domain.Game;
import domain.Player;
import infrastructure.Db.repositories.GameRepository;
import infrastructure.Db.repositories.PlayerRepository;
import infrastructure.Db.repositories.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        }
        if (isTeamExistsInSeason(teamId, season)) {
            playerRepository.addNewSeasonPlayer(Player.builder().fio(fio).team_id((short) teamId).season(season).build());
        }
    }

    private boolean isTeamExistsInSeason(int teamId, int season) {
        return teamService.isTeamExistsInSeason(teamId, season);
    }

    private boolean isPlayerExists(String fio) {
        return playerRepository.isPlayerExists(Player.builder().fio(fio).build());
    }
}


