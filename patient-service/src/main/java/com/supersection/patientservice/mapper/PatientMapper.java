package com.supersection.patientservice.mapper;

import java.time.LocalDate;

import com.supersection.patientservice.dto.PatientRequestDTO;
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

  public static Patient toModel(PatientRequestDTO patientRequestDTO) {
    Patient patient = new Patient();
    patient.setName(patientRequestDTO.getName());
    patient.setEmail(patientRequestDTO.getEmail());
    patient.setAddress(patientRequestDTO.getAddress());
    patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
    patient.setRegisteredDate(LocalDate.parse(patientRequestDTO.getRegisteredDate()));

    return patient;
  }
}
