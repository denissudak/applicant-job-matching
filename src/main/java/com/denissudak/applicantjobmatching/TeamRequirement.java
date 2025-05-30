package com.denissudak.applicantjobmatching;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TeamRequirement {

    private Integer teamMembersRequired;

    private Set<String> requiredSkills;

    public static TeamRequirement newTeamRequirement(Integer teamMembersRequired, String... requiredSkill) {
        return new TeamRequirement(teamMembersRequired, newHashSet(requiredSkill));
    }
}
