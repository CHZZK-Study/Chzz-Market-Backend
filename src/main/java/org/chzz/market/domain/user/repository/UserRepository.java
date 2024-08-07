package org.chzz.market.domain.user.repository;

import org.chzz.market.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
