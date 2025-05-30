package com.denissudak.applicantjobmatching;

import com.google.common.collect.SetMultimap;
import lombok.Getter;
import org.openstructures.flow.FlowNetwork;
import org.openstructures.flow.Node;
import org.openstructures.flow.PushRelabelMaxFlow;
import org.openstructures.flow.ValueNode;

import java.util.*;
import java.util.function.BiPredicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.requireNonNull;
import static org.openstructures.flow.ValueNode.node;

public class TeamNetwork {
    @Getter
    private final FlowNetwork flowNetwork;

    private PushRelabelMaxFlow flow;

    private TeamNetwork(FlowNetwork flowNetwork) {
        this.flowNetwork = requireNonNull(flowNetwork);
    }

    private void setFlow() {
        flow = new PushRelabelMaxFlow(flowNetwork);
    }

    /**
     * Sets feasible flow that reflects current roles assignment in the team.
     * Pushes the flow from the source to all job assignment nodes.
     * From there, the flow from each job assignment node is pushed to the team requirement node that is assigned to it.
     * Lastly flow from team requirement nodes is pushed to the sink.
     * The amount of flow from each team requirement node is equal to the number of its job assignments.
     */
    public void setFlow(SetMultimap<TeamRequirement, Applicant> teamRolesAssignment) {
        checkNotNull(teamRolesAssignment);
        checkState(flow == null, "Flow is already set");

        setFlow();
        for (TeamRequirement tr : teamRolesAssignment.keySet()) {
            for (Applicant teamMember : teamRolesAssignment.get(tr)) {
                setFlow(teamMember, tr);
            }
        }
    }

    public void setFlow(Applicant applicant, TeamRequirement tr) {
        checkNotNull(applicant);
        checkNotNull(tr);

        if (flow == null) {
            setFlow();
        }
        Node teamMemberNode = node(applicant);
        Node trNode = node(tr);
        List<Node> path = pathSearch(teamMemberNode, trNode);
        if (path.isEmpty()) {
            throw new IllegalStateException("There is not path between " + applicant + " and " + tr);
        } else {
            flow.pushFlow(1, flowNetwork.getSource(), node(applicant));
            pushOneAlongThePath(path);
            flow.pushFlow(1, trNode, flowNetwork.getSink());
        }
    }

    private List<Node> pathSearch(Node tail, Node targetHead) {
        for (Node node : flowNetwork.getSuccessors(tail)) {
            if (node.equals(targetHead)) {
                return newArrayList(tail, targetHead);
            }
        }
        return Collections.emptyList();
    }

    private void pushOneAlongThePath(List<Node> path) {
        Iterator<Node> pathIterator = path.iterator();
        Node previousNode = pathIterator.next();
        while (pathIterator.hasNext()) {
            Node nextNode = pathIterator.next();
            flow.pushFlow(1, previousNode, nextNode);
            previousNode = nextNode;
        }
    }

    /**
     * Pushes as much flow as possible from source to sink
     */
    public void preflowPush() {
        if (flow == null) {
            setFlow();
        }
        flow.preflowPush();
    }

    private void checkFlowIsSet() {
        checkState(flow != null, "Flow is not set");
    }

    public int getFlowAmount() {
        checkFlowIsSet();
        return flow.getFlowAmount();
    }

    /**
     * Team network is a network created from bipartite graph UV
     * where nodes in set U correspond of applicants and nodes in set V correspond to team requirements.
     * A node of set U is adjacent to a node in V only if an applicant has required skills.
     * The capacity of arc incident to these nodes is 1 – one team member can only reduce {@link TeamRequirement#getTeamMembersRequired()} by one.
     * Source is adjacent to all nodes in U with every arcs having capacity 1 – an applicant can only be assigned one role.
     * All nodes in V are adjacent to sink. Each arc connecting node from V with sink has capacity equal to {@link TeamRequirement#getTeamMembersRequired()}
     */
    public static TeamNetwork newTeamNetwork(BiPredicate<Applicant, Set<String>> applicantQualifications, Set<Applicant> applicants, Set<TeamRequirement> teamRequirements) {
        checkNotNull(applicantQualifications);
        checkNotNull(applicants);
        checkNotNull(teamRequirements);

        final Node source = new SourceNode();
        final Node sink = new SinkNode();
        FlowNetwork flowNetwork = new FlowNetwork(source, sink);
        for (TeamRequirement tr : teamRequirements) {
            flowNetwork.setArcCapacity(tr.getTeamMembersRequired(), node(tr), sink);
        }
        for (Applicant applicant : applicants) {
            flowNetwork.setArcCapacity(1, source, node(applicant));
            for (TeamRequirement tr : teamRequirements) {
                if (applicantQualifications.test(applicant, tr.getRequiredSkills())) {
                    Node applicantNode = node(applicant);
                    Node teamRequirementNode = node(tr);
                    flowNetwork.setArcCapacity(1, applicantNode, teamRequirementNode);
                }
            }
        }

        return new TeamNetwork(flowNetwork);
    }

    public Map<Applicant, Set<String>> getRoleAssignments() {
        Map<Applicant, Set<String>> roleAssignments = newHashMap();
        for (Node trNode : flowNetwork.getSuccessors(flowNetwork.getSink())) {
            for (Node teamMemberNode : flowNetwork.getPredecessors(flowNetwork.getSource())) {
                if (existsFlowBetweenNodes(trNode, teamMemberNode, flowNetwork)) {
                    roleAssignments.put(((ValueNode<Applicant>) teamMemberNode).getValue(), ((ValueNode<TeamRequirement>) trNode).getValue().getRequiredSkills());
                }
            }
        }

        return roleAssignments;
    }

    private static final class SourceNode implements Node {
    }

    private static final class SinkNode implements Node {
    }

    private static boolean existsFlowBetweenNodes(Node origin, Node destination, FlowNetwork teamNetwork) {
        for (Node successor : teamNetwork.getSuccessors(origin)) {
            if (successor.equals(destination)) {
                return true;
            }
        }
        return false;
    }
}