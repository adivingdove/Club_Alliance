package com.example.uclub_backend.forum.repository;



import com.example.uclub_backend.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    Optional<Club> findByName(String name);
}
