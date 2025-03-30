package com.supersection.patientservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.supersection.patientservice.dto.PatientResponseDTO;
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
}
