package com.sqspublisherconsumer.controller;

import com.sqspublisherconsumer.payload.EmployeeDto;
import com.sqspublisherconsumer.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/producer")
    @ResponseStatus(HttpStatus.OK)
    public String publishEmployeeToSqs(EmployeeDto employeeDto) throws IOException {
        return employeeService.employeeProducer(employeeDto);
    }
}
