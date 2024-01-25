package com.sqspublisherconsumer.repository;

import com.sqspublisherconsumer.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
}
