package com.tournabay.api.service;

import com.tournabay.api.exception.AppException;
import com.tournabay.api.exception.BadRequestException;
import com.tournabay.api.exception.IncorrectTournamentType;
import com.tournabay.api.exception.ResourceNotFoundException;
import com.tournabay.api.model.*;
import com.tournabay.api.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final ParticipantService participantService;

    /**
     * Update the team to the database.
     *
     * @param team The team object that is being saved.
     * @return The team object that was saved.
     */
    public Team update(Team team, Tournament tournament) {
        if (tournament instanceof TeamBasedTournament) {
            TeamBasedTournament teamBasedTournament = (TeamBasedTournament) tournament;
            Settings teamBasedTournamentSettings = teamBasedTournament.getSettings();
            Integer baseTeamSize = teamBasedTournamentSettings.getBaseTeamSize();
            Integer maxTeamSize = teamBasedTournamentSettings.getMaxTeamSize();
            if (team.getParticipants().size() < baseTeamSize || team.getParticipants().size() > maxTeamSize) {
                throw new BadRequestException("Team size must be between " + baseTeamSize + " and " + maxTeamSize);
            }
            return teamRepository.save(team);
        }
        throw new BadRequestException("Invalid tournament type!");
    }

    /**
     * Create a team for a team based tournament
     *
     * @param name           The name of the team
     * @param captainId      The id of the captain of the team
     * @param participantIds List of participant ids that will be in the team
     * @param seed           The seed of the team.
     * @param teamStatus     The status of the team.
     * @param tournament     The tournament that the team is being created for.
     * @return A new team is being returned.
     */
    public Team createTeam(String name, Long captainId, List<Long> participantIds, Seed seed, TeamStatus teamStatus, Tournament tournament) {
        if (tournament instanceof TeamBasedTournament) {
            TeamBasedTournament teamBasedTournament = (TeamBasedTournament) tournament;
            // Check if the team name is unique
            if (checkForUniqueTeamName(name, teamBasedTournament))
                throw new BadRequestException("Team name already exists!");
            // Check if the team size is valid
            Settings settings = teamBasedTournament.getSettings();
            Integer maxTeamSize = settings.getMaxTeamSize();
            if (participantIds.size() > maxTeamSize)
                throw new BadRequestException("Team size cannot be greater than " + maxTeamSize);
            // Check if the participants are not in other teams
            List<Participant> participants = participantService.getAllByIds(new HashSet<>(participantIds), tournament);
            if (checkForDuplicatedParticipants(participants, teamBasedTournament))
                throw new BadRequestException("One or more participant is already in other team!");
            // First participant in the list is the captain
            Participant captain = participantService.getById(captainId, tournament);
            Team team = Team.builder()
                    .name(name)
                    .seed(seed)
                    .status(teamStatus)
                    .captain(captain)
                    .participants(new HashSet<>(participants))
                    .tournament(teamBasedTournament)
                    .build();
            return teamRepository.save(team);
        }
        throw new BadRequestException("Invalid tournament type!");
    }

    /**
     * If the tournament is a team based tournament, then delete the team and set all the participants' team to null.
     *
     * @param tournament The tournament that the team is being removed from.
     * @param team       The team to be removed.
     * @return The team that was removed.
     */
    @SuppressWarnings("UnusedReturnValue")
    public Team removeTeam(Tournament tournament, Team team) {
        if (tournament instanceof TeamBasedTournament) {
            teamRepository.delete(team);
            return team;
        }
        throw new BadRequestException("Invalid tournament type!");
    }

    /**
     * If the team exists, return it, otherwise throw an exception.
     *
     * @param id The id of the team to be found.
     * @return A team object
     */
    public Team findById(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Team not found!"));
    }

    /**
     * If the tournament is a team based tournament, return the team with the given id, otherwise throw an exception.
     *
     * @param teamId     The id of the team we want to get.
     * @param tournament The tournament that the team belongs to.
     * @return A team object
     */
    public Team getById(Long teamId, Tournament tournament) {
        if (teamId == null)
            throw new BadRequestException("Team id cannot be null!");
        if (tournament == null)
            throw new AppException("Tournament cannot be null!");
        if (tournament instanceof TeamBasedTournament) {
            return teamRepository.findById(teamId).orElseThrow(() -> new ResourceNotFoundException("Team not found!"));
        }
        throw new BadRequestException("Invalid tournament type!");
    }

    /**
     * If the tournament is a team based tournament, return a list of teams that are in the tournament and have an id that
     * is in the list of team ids.
     *
     * @param teamIds    A list of team ids
     * @param tournament The tournament to get the teams from.
     * @return A list of teams
     */
    public List<Team> getAllByIds(List<Long> teamIds, Tournament tournament) {
        if (tournament instanceof TeamBasedTournament) {
            return ((TeamBasedTournament) tournament).getTeams()
                    .stream()
                    .filter(team -> teamIds.contains(team.getId()))
                    .collect(Collectors.toList());
        }
        throw new IncorrectTournamentType("Invalid tournament type!");
    }

    /**
     * It updates a team in a tournament
     *
     * @param tournament     The tournament that the team belongs to.
     * @param team           The team to be updated
     * @param name           The name of the team
     * @param captainId      The id of the captain of the team
     * @param participantIds A list of participant IDs that will be added to the team.
     * @param seed           The seed of the team.
     * @param status         The status of the team.
     * @return A team object
     */
    public Team updateTeam(Tournament tournament, Team team, String name, Long captainId, List<Long> participantIds, Seed seed, TeamStatus status) {
        if (tournament instanceof TeamBasedTournament) {
            TeamBasedTournament teamBasedTournament = (TeamBasedTournament) tournament;
            if (!team.getName().equals(name) && checkForUniqueTeamName(name, teamBasedTournament))
                throw new BadRequestException("Team name already exists!");
            // Check if the team size is valid
            Settings settings = teamBasedTournament.getSettings();
            Integer maxTeamSize = settings.getMaxTeamSize();
            if (participantIds.size() > maxTeamSize)
                throw new BadRequestException("Team size cannot be greater than " + maxTeamSize);
            // Check if the participants are not in other teams
            List<Participant> participants = participantService.getAllByIds(new HashSet<>(participantIds), tournament);
            Set<Participant> currentParticipants = team.getParticipants();
            List<Participant> newParticipants = participants.stream()
                    .filter(participant -> !currentParticipants.contains(participant))
                    .collect(Collectors.toList());
            if (checkForDuplicatedParticipants(newParticipants, teamBasedTournament))
                throw new BadRequestException("One or more participant is already in other team!");
            team.getParticipants().forEach(participant -> participant.setTeam(null));
            team.getCaptain().setTeam(null);
            Participant captain = participantService.getById(captainId, tournament);
            team.setName(name);
            team.setCaptain(captain);
            team.setParticipants(new HashSet<>(participants));
            team.setSeed(seed);
            team.setStatus(status);
            return teamRepository.save(team);
        }
        throw new BadRequestException("Invalid tournament type!");
    }

    /**
     * If the tournament is a team based tournament, then return the team that has a participant with the given user id,
     * otherwise throw a bad request exception.
     *
     * @param id The id of the user that we want to find the team for.
     * @param tournament The tournament object that we want to get the team from.
     * @return A team
     */
    public Team getTeamByParticipantOsuIdWithoutThrow(Long id, Tournament tournament) {
        if (tournament instanceof TeamBasedTournament) {
            TeamBasedTournament teamBasedTournament = (TeamBasedTournament) tournament;
            return teamBasedTournament.getTeams()
                    .stream()
                    .filter(team -> team.getParticipants()
                            .stream()
                            .anyMatch(participant -> participant.getUser().getOsuId().equals(id))
                            ||
                            team.getCaptain().getUser().getOsuId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
        throw new BadRequestException("Invalid tournament type!");
    }

    /**
     * Check if any of the participants in the list of participants are already in the tournament's team
     *
     * @param participants The list of participants that are being added to the team.
     * @param tournament   The tournament to check for duplicate participants in.
     * @return A boolean value.
     */
    private boolean checkForDuplicatedParticipants(List<Participant> participants, TeamBasedTournament tournament) {
        return tournament.getTeams()
                .stream()
                .anyMatch(team -> team.getParticipants()
                        .stream()
                        .anyMatch(participant -> participants
                                .stream()
                                .anyMatch(participant1 -> participant1.getId().equals(participant.getId())
                                )));
    }

    /**
     * Check if any of the teams in the tournament have the same name as the one passed in.
     *
     * @param name       The name of the team to be created
     * @param tournament The tournament that the team is being added to.
     * @return A boolean value.
     */
    private boolean checkForUniqueTeamName(String name, TeamBasedTournament tournament) {
        return tournament.getTeams()
                .stream()
                .anyMatch(team -> team.getName().equals(name));
    }
}
