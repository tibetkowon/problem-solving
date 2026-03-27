package com.problemsolving.core.repository;

import com.problemsolving.core.domain.userproblem.entity.UserProblemChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProblemChoiceRepository extends JpaRepository<UserProblemChoice, Long> {

    List<UserProblemChoice> findByUserProblemId(Long userProblemId);
}
