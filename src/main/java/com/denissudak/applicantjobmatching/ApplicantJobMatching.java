package com.denissudak.applicantjobmatching;

import java.util.Map;
import java.util.Set;

import static com.denissudak.applicantjobmatching.Applicant.newApplicant;
import static com.denissudak.applicantjobmatching.TeamRequirement.newTeamRequirement;
import static com.google.common.collect.Sets.newHashSet;

public class ApplicantJobMatching {

    public static void main(String[] args) {
        /*
         * Below is related to Part 1: https://denissudak.substack.com/p/applying-matching-algorithms-to-real
         */
        Applicant a1 = newApplicant("applicant-1", "skill-1", "skill-2");
        Applicant a2 = newApplicant("applicant-2", "skill-1", "skill-2", "skill-3");
        Applicant a3 = newApplicant("applicant-3", "skill-2", "skill-3");

        TeamRequirement tr1 = newTeamRequirement(1, "skill-1");
        TeamRequirement tr2 = newTeamRequirement(2, "skill-2");
        TeamRequirement tr3 = newTeamRequirement(2, "skill-3");

        TeamNetwork teamNetwork = TeamNetwork.newTeamNetwork(Applicant::hasSkills, newHashSet(a1, a2, a3), newHashSet(tr1, tr2, tr3));
        teamNetwork.preflowPush();
        Map<Applicant, Set<String>> roleAssignments = teamNetwork.getRoleAssignments();
        roleAssignments.forEach((applicant, roleSkills) -> System.out.printf("%s : %s%n", applicant.getName(), roleSkills));

        /*
         * Below is related to Part 2: https://denissudak.substack.com/p/applying-matching-algorithms-to-real-8e7
         */
        TeamRequirementsAnalyser teamRequirementsAnalyser = new TeamRequirementsAnalyser();
        Set<Set<String>> rolesInDemand = teamRequirementsAnalyser.getRolesInDemand(newHashSet(tr1, tr2, tr3), roleAssignments, Applicant::hasSkills);
        System.out.println("Roles in demand: " + rolesInDemand);
    }
}
