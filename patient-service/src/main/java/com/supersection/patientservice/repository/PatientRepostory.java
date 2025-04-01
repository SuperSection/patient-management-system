package com.supersection.patientservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supersection.patientservice.model.Patient;

@Repository
public interface PatientRepostory extends JpaRepository<Patient, UUID> {
  boolean existsByEmail(String email);

  // Check if there is another patient in the database
  // with the same Email as we are passing, but with a different ID
  boolean existsByEmailAndIdNot(String email, UUID id);
}
