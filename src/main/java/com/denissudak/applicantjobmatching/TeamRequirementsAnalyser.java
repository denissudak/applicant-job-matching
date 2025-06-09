package com.denissudak.applicantjobmatching;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import org.open_structures.matching.Matching;
import org.openstructures.flow.PushRelabelMaxFlow;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;


public class TeamRequirementsAnalyser {

    /**
     * The role ({@link TeamRequirement#getRequiredSkills()}) is in demand when a team member who has required skills can join the team
     * without replacing anyone who is already on the team.
     * If a team requirement requires more team members than are currently assigned to it then obviously it's in demand.
     * If a team requirement is satisfied, that is, it has the number of team members that it requires, then we need to check whether
     * at least one of these team members could be doing something else. We do that in the following way:
     * We add 1 to the capacity of the arc that emanates from source and terminates at each node that corresponds to team members linked to the team requirement in question.
     * If that increases the flow, then at least one of these team members found another role (without kicking anyone out) and so we can say that the team requirement is in demand.
     */
    public Set<Set<String>> getRolesInDemand(Set<TeamRequirement> teamRequirements, Map<Applicant, Set<String>> roleAssignments, BiPredicate<Applicant, Set<String>> qualificationsPredicate) {
        checkNotNull(teamRequirements);
        checkNotNull(roleAssignments);

        Matching<Applicant, Set<String>> teamNetwork = Matching.newMatching(
                qualificationsPredicate,
                roleAssignments.keySet(),
                teamRequirements.stream().collect(Collectors.toMap(TeamRequirement::getRequiredSkills, TeamRequirement::getTeamMembersRequired))
        );
        SetMultimap<TeamRequirement, Applicant> rolesAssignment = getTeamRolesAssignment(teamRequirements, roleAssignments);
        rolesAssignment.keySet().forEach(tr -> rolesAssignment.get(tr).forEach(contact -> teamNetwork.setMatch(contact, tr.getRequiredSkills())));

        Set<Set<String>> soughtAfterSkills = newHashSet();
        for (TeamRequirement tr : teamRequirements) {
            if (tr.getTeamMembersRequired() == rolesAssignment.get(tr).size()) { // team requirement is satisfied
                final int flowBefore = teamNetwork.getFlowAmount();
                final PushRelabelMaxFlow.State flowState = teamNetwork.getState();
                for (Applicant applicant : rolesAssignment.get(tr)) {
                    teamNetwork.increaseUCount(applicant, 1); // count
                }
                teamNetwork.findMatching();
                final int flowAfter = teamNetwork.getFlowAmount();
                if (flowAfter > flowBefore) {
                    soughtAfterSkills.add(tr.getRequiredSkills());
                }
                teamNetwork.restore(flowState);
            } else {
                soughtAfterSkills.add(tr.getRequiredSkills());
            }
        }
        return soughtAfterSkills;
    }

    private static SetMultimap<TeamRequirement, Applicant> getTeamRolesAssignment(Set<TeamRequirement> teamRequirements, Map<Applicant, Set<String>> jobAssignments) {
        SetMultimap<TeamRequirement, Applicant> matching = HashMultimap.create();
        for (Applicant contact : jobAssignments.keySet()) {
            boolean matched = false;
            Iterator<TeamRequirement> teamRequirementIterator = teamRequirements.iterator();
            while (!matched && teamRequirementIterator.hasNext()) {
                TeamRequirement tr = teamRequirementIterator.next();
                if (jobAssignments.get(contact).equals(tr.getRequiredSkills())) {
                    matching.put(tr, contact);
                    matched = true;
                }
            }
            if (!matched) {
                throw new IllegalArgumentException("Job assignment " + contact + " can not be matched to any of the team requirements");
            }
        }

        return matching;
    }
}
