package com.example.uclub_backend.club.repository;

import com.example.uclub_backend.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {
    Club findByName(String name);
}
