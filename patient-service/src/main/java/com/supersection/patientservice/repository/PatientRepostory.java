package com.supersection.patientservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supersection.patientservice.model.Patient;

@Repository
public interface PatientRepostory extends JpaRepository<Patient, UUID> {

}
