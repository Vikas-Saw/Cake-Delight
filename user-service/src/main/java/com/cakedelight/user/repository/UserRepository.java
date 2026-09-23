package com.cakedelight.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cakedelight.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

}