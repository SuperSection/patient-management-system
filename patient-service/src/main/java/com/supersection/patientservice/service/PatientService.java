package com.supersection.patientservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.supersection.patientservice.dto.PatientRequestDTO;
import com.supersection.patientservice.dto.PatientResponseDTO;
import com.supersection.patientservice.exception.EmailAlreadyExistsException;
import com.supersection.patientservice.mapper.PatientMapper;
import com.supersection.patientservice.model.Patient;
import com.supersection.patientservice.repository.PatientRepostory;

@Service
public class PatientService {
  private final PatientRepostory patientRepostory;

  public PatientService(PatientRepostory patientRepostory) {
    this.patientRepostory = patientRepostory;
  }

  public List<PatientResponseDTO> getPatients() {
    List<Patient> patients = patientRepostory.findAll();

    return patients.stream()
        .map(patient -> PatientMapper.toDTO(patient)).toList();
  }

  public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
    if (patientRepostory.existsByEmail(patientRequestDTO.getEmail())) {
      throw new EmailAlreadyExistsException(
          "A patient with this email already exists: " + patientRequestDTO.getEmail());
    }

    Patient newPatient = patientRepostory.save(
        PatientMapper.toModel(patientRequestDTO)
      );
    return PatientMapper.toDTO(newPatient);
  }
}
