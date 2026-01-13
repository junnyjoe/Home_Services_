package com.home.services.repository;

import com.home.services.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNom(String nom);

    List<Category> findByActiveTrue();

    boolean existsByNom(String nom);
}
