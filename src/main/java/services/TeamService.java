package services;

import domain.Team;
import infrastructure.Db.repositories.TeamRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Getter
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    private final Logger logger = LoggerFactory.getLogger(TeamService.class);

    public void createTeam(int teamId, String nameTeam) {
        if (!teamRepository.isExistsTeam((short) teamId)) {
            teamRepository.createTeam(Team.builder().id((short) teamId).title(nameTeam).build());
        }
    }

    public void updateTeam(int teamId, StringBuilder pointWin, StringBuilder teamSetWin, StringBuilder pointLost, StringBuilder teamSetLost, int teamWin, int teamLost, int season) {
        short teamIdSh = (short) teamId;
        Team team = Team.builder().
                id(teamIdSh).
                pointsWon(pointWin).
                setWon(teamSetWin).
                pointsLost(pointLost).
                setLost(teamSetLost).
                wins(teamWin).
                loses(teamLost).
                season(season).
                build();
        if (!teamRepository.isExistsTeamInSeason(teamIdSh, season)) {
            teamRepository.addTeamInSeason(team);
            logger.info("Инфо о команде добавлено");
        } else {
            teamRepository.updateTeam(team);
            logger.info("Инфо о команде изменено");
        }
    }

    public boolean isTeamExistsInSeason(int teamId, int season) {
        return teamRepository.isExistsTeamInSeason((short) teamId, season);
    }
}

