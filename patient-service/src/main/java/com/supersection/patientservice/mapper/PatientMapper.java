package com.supersection.patientservice.mapper;

import com.supersection.patientservice.dto.PatientResponseDTO;
import com.supersection.patientservice.model.Patient;

public class PatientMapper {

  public static PatientResponseDTO toDTO(Patient patient) {
    PatientResponseDTO patientDTO = new PatientResponseDTO();
    patientDTO.setId(patient.getId().toString());
    patientDTO.setName(patient.getName());
    patientDTO.setEmail(patient.getEmail());
    patientDTO.setAddress(patient.getAddress());
    patientDTO.setDateOfBirth(patient.getDateOfBirth().toString());

    return patientDTO;
  }
}
