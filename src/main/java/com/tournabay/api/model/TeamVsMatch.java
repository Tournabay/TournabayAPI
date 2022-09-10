package com.tournabay.api.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;
import javax.validation.constraints.NotNull;

@SuperBuilder
@Entity
@DynamicUpdate
@NoArgsConstructor
@Getter
@Setter
public class TeamVsMatch extends Match {

    @OneToOne
    private Team redTeam;

    @OneToOne
    private Team blueTeam;

    @OneToOne
    private Team winner;

    @OneToOne
    private Team loser;

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id"
    )
    @ManyToOne
    @NotNull
    private TeamBasedTournament tournament;

    @PreRemove
    private void preRemove() {
        this.tournament.getMatches().remove(this);
        this.tournament = null;
    }
}