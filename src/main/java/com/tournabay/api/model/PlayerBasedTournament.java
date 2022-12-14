package com.tournabay.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class PlayerBasedTournament extends Tournament {

    @OneToMany(mappedBy = "tournament", orphanRemoval = true, cascade = {CascadeType.ALL})
    private List<ParticipantVsMatch> matches;

}
