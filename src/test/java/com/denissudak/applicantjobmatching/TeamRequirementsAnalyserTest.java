package com.denissudak.applicantjobmatching;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import static com.denissudak.applicantjobmatching.Applicant.newApplicant;
import static com.denissudak.applicantjobmatching.TeamRequirement.newTeamRequirement;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TeamRequirementsAnalyserTest {

    private final String skill1 = "skill1", skill2 = "skill2", skill3 = "skill3";

    private final Applicant applicant1 = newApplicant("applicant1", skill1, skill2);
    private final Applicant applicant2 = newApplicant("applicant2", skill1, skill2, skill3);

    @Mock
    private BiPredicate<Applicant, Set<String>> qualificationsPredicate;

    private final TeamRequirementsAnalyser teamRequirementsAnalyser = new TeamRequirementsAnalyser();

    /**
     * It should return roles in all team requirements if nobody is on the team
     */
    @Test
    public void shouldGetRolesInDemandWithEmptyQualificationMap() {
        // given
        TeamRequirement tr1 = newTeamRequirement(2, skill1);
        TeamRequirement tr2 = newTeamRequirement(1, skill2, skill3);

        // when
        Set<Set<String>> result = teamRequirementsAnalyser.getRolesInDemand(
                newHashSet(tr1, tr2), newHashMap(), qualificationsPredicate
        );

        // then
        assertThat(result).hasSize(2).contains(newHashSet(skill1), newHashSet(skill2, skill3));
    }

    /**
     * It should return the only available role.
     * In this test team member can satisfy only one team requirement.
     */
    @Test
    public void shouldGetRolesInDemandWithOneQualifiedContact() {
        // given
        TeamRequirement tr1 = newTeamRequirement(1, skill1);
        TeamRequirement tr2 = newTeamRequirement(2, skill2, skill3);

        when(qualificationsPredicate.test(applicant1, newHashSet(skill1))).thenReturn(true);

        // when
        Set<Set<String>> result = teamRequirementsAnalyser.getRolesInDemand(
                newHashSet(tr1, tr2),
                Map.of(applicant1, newHashSet(skill1)),
                qualificationsPredicate
        );

        // then
        assertThat(result).hasSize(1).contains(newHashSet(skill2, skill3));
    }

    /**
     * It should return the both available roles.
     * In this test team member can satisfy only one team requirement, but it requires more than one team member
     */
    @Test
    public void shouldGetRolesInDemandWithOneQualifiedContact2() {
        // given
        TeamRequirement tr1 = newTeamRequirement(2, skill1);
        TeamRequirement tr2 = newTeamRequirement(1, skill2, skill3);

        when(qualificationsPredicate.test(applicant1, newHashSet(skill1))).thenReturn(true);

        // when
        Set<Set<String>> result = teamRequirementsAnalyser.getRolesInDemand(
                newHashSet(tr1, tr2),
                Map.of(applicant1, newHashSet(skill1)),
                qualificationsPredicate
        );

        // then
        assertThat(result).hasSize(2).contains(newHashSet(skill1), newHashSet(skill2, skill3));
    }

    @Test
    public void shouldGetRolesInDemandWithOneQualifiedContact3() {
        // given
        TeamRequirement tr1 = newTeamRequirement(1, skill1);
        TeamRequirement tr2 = newTeamRequirement(2, skill2, skill3);

        when(qualificationsPredicate.test(applicant1, newHashSet(skill1))).thenReturn(true);
        when(qualificationsPredicate.test(applicant1, newHashSet(skill2, skill3))).thenReturn(true);

        // when
        Set<Set<String>> result = teamRequirementsAnalyser.getRolesInDemand(
                newHashSet(tr1, tr2),
                Map.of(applicant1, newHashSet(skill1)),
                qualificationsPredicate
        );

        // then
        assertThat(result).hasSize(2).contains(newHashSet(skill1), newHashSet(skill2, skill3));
    }

    /**
     * It should return all roles.
     * In this test both team members could be reassigned to other available positions
     */
    @Test
    public void shouldGetAllRolesWhenTeamMembersCanBeReassigned() {
        // given
        TeamRequirement tr1 = newTeamRequirement(2, skill1);
        TeamRequirement tr2 = newTeamRequirement(1, skill2);

        when(qualificationsPredicate.test(applicant1, newHashSet(skill1))).thenReturn(true);
        when(qualificationsPredicate.test(applicant2, newHashSet(skill1))).thenReturn(true);
        when(qualificationsPredicate.test(applicant2, newHashSet(skill2))).thenReturn(true);

        // when
        Set<Set<String>> result = teamRequirementsAnalyser.getRolesInDemand(
                newHashSet(tr1, tr2),
                Map.of(
                        applicant1, newHashSet(skill1),
                        applicant2, newHashSet(skill1)
                ),
                qualificationsPredicate
        );

        // then
        assertThat(result).hasSize(2).contains(newHashSet(skill1), newHashSet(skill2));
    }

    @Test
    public void shouldReturnEmptyResultIfNobodyCanBeAddedToTheTeam() {
        // given
        TeamRequirement tr1 = newTeamRequirement(1, skill1);
        TeamRequirement tr2 = newTeamRequirement(1, skill2);

        when(qualificationsPredicate.test(applicant1, newHashSet(skill1))).thenReturn(true);
        when(qualificationsPredicate.test(applicant2, newHashSet(skill2))).thenReturn(true);

        // when
        Set<Set<String>> result = teamRequirementsAnalyser.getRolesInDemand(
                newHashSet(tr1, tr2),
                Map.of(
                        applicant1, newHashSet(skill1),
                        applicant2, newHashSet(skill2)
                ),
                qualificationsPredicate
        );

        // then
        assertThat(result).isEmpty();
    }
}
