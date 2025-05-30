package com.denissudak.applicantjobmatching;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openstructures.flow.FlowNetwork;
import org.openstructures.flow.Node;

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import static com.denissudak.applicantjobmatching.Applicant.newApplicant;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openstructures.flow.ValueNode.node;

@ExtendWith(MockitoExtension.class)
public class TeamNetworkTest {

    private Applicant teamMember1, teamMember2, teamMember3;

    private final String skill1 = "skill1", skill2 = "skill2", skill3 = "skill3";

    private TeamRequirement tr1, tr2, tr3;

    private final BiPredicate<Applicant, Set<String>> applicantQualifications = Applicant::hasSkills;

    @BeforeEach
    public void setUp() {
        tr1 = TeamRequirement.newTeamRequirement(1, skill1);
        tr2 = TeamRequirement.newTeamRequirement(2, skill2);
        tr3 = TeamRequirement.newTeamRequirement(2, skill3);

        teamMember1 = newApplicant("applicant1", skill1, skill2);
        teamMember2 = newApplicant("applicant2", skill1, skill2, skill3);
        teamMember3 = newApplicant("applicant3", skill2, skill3);
    }

    @Test
    public void buildTeamNetwork() {
        // when
        TeamNetwork teamNetwork = TeamNetwork.newTeamNetwork(applicantQualifications, newHashSet(teamMember1, teamMember2, teamMember3), newHashSet(tr1, tr2, tr3));

        // then
        assertThat(teamNetwork).isNotNull();

        // and
        FlowNetwork flowNetwork = teamNetwork.getFlowNetwork();
        assertThat(flowNetwork).isNotNull();
        assertThat(flowNetwork.getSource()).isNotNull();
        assertThat(flowNetwork.getSink()).isNotNull();

        Node source = flowNetwork.getSource();
        Node sink = flowNetwork.getSink();

        assertThat(flowNetwork.getArcCapacity(source, node(teamMember1))).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(source, node(teamMember2))).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(source, node(teamMember3))).isEqualTo(1);

        assertThat(flowNetwork).is(containsPathBetween(node(teamMember1), node(tr1)));
        assertThat(flowNetwork).is(containsPathBetween(node(teamMember1), node(tr2)));

        assertThat(flowNetwork).is(containsPathBetween(node(teamMember2), node(tr1)));
        assertThat(flowNetwork).is(containsPathBetween(node(teamMember2), node(tr2)));
        assertThat(flowNetwork).is(containsPathBetween(node(teamMember2), node(tr3)));

        assertThat(flowNetwork).is(containsPathBetween(node(teamMember3), node(tr2)));
        assertThat(flowNetwork).is(containsPathBetween(node(teamMember3), node(tr3)));

        assertThat(flowNetwork.getArcCapacity(node(tr1), sink)).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(node(tr2), sink)).isEqualTo(2);
        assertThat(flowNetwork.getArcCapacity(node(tr3), sink)).isEqualTo(2);
    }

    @Test
    public void shouldSetFlow() {
        // given
        TeamNetwork teamNetwork = TeamNetwork.newTeamNetwork(applicantQualifications, newHashSet(teamMember1, teamMember2, teamMember3), newHashSet(tr1, tr2, tr3));
        SetMultimap<TeamRequirement, Applicant> teamRolesAssignments = HashMultimap.create();
        teamRolesAssignments.put(tr2, teamMember1);
        teamRolesAssignments.put(tr2, teamMember2);
        teamRolesAssignments.put(tr3, teamMember3);

        // when
        teamNetwork.setFlow(teamRolesAssignments);

        // then
        FlowNetwork flowNetwork = teamNetwork.getFlowNetwork();
        assertThat(flowNetwork).isNotNull();
        assertThat(flowNetwork.getArcCapacity(node(teamMember1), flowNetwork.getSource())).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(node(teamMember2), flowNetwork.getSource())).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(node(teamMember3), flowNetwork.getSource())).isEqualTo(1);

        assertThat(flowNetwork.getArcCapacity(flowNetwork.getSink(), node(tr2))).isEqualTo(2);
        assertThat(flowNetwork.getArcCapacity(flowNetwork.getSink(), node(tr3))).isEqualTo(1);

        assertThat(flowNetwork).is(containsPathBetween(node(tr2), node(teamMember1)));
        assertThat(flowNetwork).is(containsPathBetween(node(tr2), node(teamMember2)));
        assertThat(flowNetwork).is(containsPathBetween(node(tr3), node(teamMember3)));
    }

    /**
     * It should throw exception if attempting to set the flow that is impossible.
     * In this test we are trying to push flow from team member to skill 3, although they are not connected.
     */
    @Test
    public void shouldThrowExceptionIfSettingInvalidFlow() {
        // given
        TeamNetwork teamNetwork = TeamNetwork.newTeamNetwork(applicantQualifications, newHashSet(teamMember1, teamMember2, teamMember3), newHashSet(tr1, tr2, tr3));
        SetMultimap<TeamRequirement, Applicant> teamRolesAssignments = HashMultimap.create();
        teamRolesAssignments.put(tr1, teamMember3);

        // when and then expect exception
        assertThrows(IllegalStateException.class, () -> {
            teamNetwork.setFlow(teamRolesAssignments);
        });
    }

    @Test
    public void shouldSetFlowBetweenTeamMemberAndRequirement() {
        // given
        TeamNetwork teamNetwork = TeamNetwork.newTeamNetwork(applicantQualifications, newHashSet(teamMember1, teamMember2, teamMember3), newHashSet(tr1, tr2, tr3));

        // when
        teamNetwork.setFlow(teamMember2, tr2);
        teamNetwork.setFlow(teamMember3, tr3);

        // then
        FlowNetwork flowNetwork = teamNetwork.getFlowNetwork();
        assertThat(flowNetwork).isNotNull();
        assertThat(flowNetwork.getArcCapacity(node(teamMember1), flowNetwork.getSource())).isZero();
        assertThat(flowNetwork.getArcCapacity(node(teamMember2), flowNetwork.getSource())).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(node(teamMember3), flowNetwork.getSource())).isEqualTo(1);

        assertThat(flowNetwork.getArcCapacity(flowNetwork.getSink(), node(tr2))).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(flowNetwork.getSink(), node(tr3))).isEqualTo(1);

        assertThat(flowNetwork).is(containsPathBetween(node(tr2), node(teamMember2)));
        assertThat(flowNetwork).is(containsPathBetween(node(tr3), node(teamMember3)));
    }

    @Test
    public void shouldSetFlowBetweenTeamMemberAndRequirementInAdditionToExistingFlow() {
        // given
        TeamNetwork teamNetwork = TeamNetwork.newTeamNetwork(applicantQualifications, newHashSet(teamMember1, teamMember2, teamMember3), newHashSet(tr1, tr2, tr3));
        SetMultimap<TeamRequirement, Applicant> teamRolesAssignments = HashMultimap.create();
        teamRolesAssignments.put(tr2, teamMember2);
        teamRolesAssignments.put(tr3, teamMember3);

        // when
        teamNetwork.setFlow(teamRolesAssignments);
        teamNetwork.setFlow(teamMember1, tr2);

        // then
        FlowNetwork flowNetwork = teamNetwork.getFlowNetwork();
        assertThat(flowNetwork).isNotNull();
        assertThat(flowNetwork.getArcCapacity(node(teamMember1), flowNetwork.getSource())).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(node(teamMember2), flowNetwork.getSource())).isEqualTo(1);
        assertThat(flowNetwork.getArcCapacity(node(teamMember3), flowNetwork.getSource())).isEqualTo(1);

        assertThat(flowNetwork.getArcCapacity(flowNetwork.getSink(), node(tr2))).isEqualTo(2);
        assertThat(flowNetwork.getArcCapacity(flowNetwork.getSink(), node(tr3))).isEqualTo(1);

        assertThat(flowNetwork).is(containsPathBetween(node(tr2), node(teamMember1)));
        assertThat(flowNetwork).is(containsPathBetween(node(tr2), node(teamMember2)));
        assertThat(flowNetwork).is(containsPathBetween(node(tr3), node(teamMember3)));
    }


    @Test
    public void shouldPushAsMuchFlowAsPossibleFromSourceToSink() {
        // given
        TeamNetwork teamNetwork = TeamNetwork.newTeamNetwork(applicantQualifications, newHashSet(teamMember1, teamMember2, teamMember3), newHashSet(tr1, tr2, tr3));

        // when
        teamNetwork.preflowPush();

        // then
        assertThat(teamNetwork.getFlowAmount()).isEqualTo(3);

        // and given
        teamMember1.setSkills(newHashSet(skill2));
        teamMember2.setSkills(newHashSet(skill1, skill2, skill3));
        teamMember3.setSkills(newHashSet(skill2));
        tr1 = TeamRequirement.newTeamRequirement(1, skill1);
        tr2 = TeamRequirement.newTeamRequirement(1, skill2);
        tr3 = TeamRequirement.newTeamRequirement(1, skill3);
        teamNetwork = TeamNetwork.newTeamNetwork(applicantQualifications, newHashSet(teamMember1, teamMember2, teamMember3), newHashSet(tr1, tr2, tr3));

        // when
        teamNetwork.preflowPush();

        // then
        assertThat(teamNetwork.getFlowAmount()).isEqualTo(2);
    }

    @Test
    public void shouldGetRoleAssignments() {
        // given
        TeamNetwork teamNetwork = TeamNetwork.newTeamNetwork(applicantQualifications, newHashSet(teamMember1, teamMember2, teamMember3), newHashSet(tr1, tr2, tr3));
        teamNetwork.setFlow(teamMember1, tr2);
        teamNetwork.setFlow(teamMember2, tr2);
        teamNetwork.setFlow(teamMember3, tr3);

        // when
        Map<Applicant, Set<String>> result = teamNetwork.getRoleAssignments();

        // then
        assertThat(result).isNotNull();
        assertThat(result.get(teamMember1)).isEqualTo(tr2.getRequiredSkills());
        assertThat(result.get(teamMember2)).isEqualTo(tr2.getRequiredSkills());
        assertThat(result.get(teamMember3)).isEqualTo(tr3.getRequiredSkills());
    }

    private static Condition<? super FlowNetwork> containsPathBetween(Node tail, Node head) {
        return new Condition<>(flowNetwork -> pathSearch(flowNetwork, tail, head),
                "Path between " + tail + " and " + head);
    }

    private static boolean pathSearch(FlowNetwork flowNetwork, Node tail, Node targetHead) {
        for (Node node : flowNetwork.getSuccessors(tail)) {
            if (node.equals(targetHead)) {
                return true;
            }
        }
        return false;
    }

}
