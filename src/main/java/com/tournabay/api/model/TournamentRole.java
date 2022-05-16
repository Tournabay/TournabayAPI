package com.tournabay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TournamentRole {

    public TournamentRole(String name, Tournament tournament, boolean isProtected, boolean isHidden, int position) {
        this.name = name;
        this.tournament = tournament;
        this.isProtected = isProtected;
        this.isHidden = isHidden;
        this.position = position;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @Lob
    private String description;

    @NotNull
    private Boolean isProtected;

    @NotNull
    private Boolean isHidden;

    @NotNull
    private Integer position;

    @JsonIgnore
    @NotNull
    @ManyToOne
    private Tournament tournament;

    @PreRemove
    private void preRemove() {
        if (tournament != null) {
            tournament.getRoles().remove(this);
        }
    }
}
