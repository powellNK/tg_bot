package services;

import domain.Team;
import infrastructure.Db.repositories.TeamRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

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

    public void updateTeam(int teamId, StringBuilder pointWin, StringBuilder teamSetWin, StringBuilder pointLost, StringBuilder teamSetLost, int teamWin, int teamLost, int pointsPerSeason, int season) {
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
                pointsPerSeason(pointsPerSeason).
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

    public List<Team> getTableResult(int season) {
        return teamRepository.getTableResult(season);
    }

    public List<Team> getTeamsFromSeason(int season) {
        return teamRepository.getTeamsFromSeason(season);
    }

    public List<Team> getStatisticsTeam(int season, short teamId) {
        return teamRepository.getStatisticsTeam(season,teamId);
    }

    public List<Team> getFullStatistic() {
        return teamRepository.getFullStatistic();
    }
}

