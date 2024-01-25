package com.sqspublisherconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sqspublisherconsumer.payload.EmployeeDto;

import java.io.IOException;

public interface EmployeeService {

    String employeeProducer(EmployeeDto employeeDto) throws IOException;
}
