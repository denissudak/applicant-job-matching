package com.denissudak.applicantjobmatching;

import lombok.*;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

@EqualsAndHashCode
@AllArgsConstructor
public class Applicant {

    @Getter
    private String name;

    @Setter
    private Set<String> skills;

    public boolean hasSkills(Set<String> skills) {
        checkNotNull(skills);

        return this.skills.containsAll(skills);
    }

    public static Applicant newApplicant(String name, String... requiredSkill) {
        return new Applicant(name, newHashSet(requiredSkill));
    }
}