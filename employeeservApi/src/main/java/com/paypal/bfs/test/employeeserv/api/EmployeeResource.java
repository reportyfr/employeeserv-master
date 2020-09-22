package com.paypal.bfs.test.employeeserv.api;

import com.paypal.bfs.test.employeeserv.api.model.Employee;
import com.paypal.bfs.test.employeeserv.api.model.EmployeeGeneralResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * Interface for employee resource operations.
 */
public interface EmployeeResource {

    /**
     * Retrieves the {@link Employee} resource by id.
     *
     * @param id employee id.
     * @return {@link Employee} resource.
     */
    @GetMapping("/v1/bfs/employees/{id}")
    ResponseEntity<Employee> employeeGetById(@PathVariable("id") int id);

    /**
     * create new employee
     *
     * @param ee Employee Entity
     * @return {@link EmployeeGeneralResponse} resource.
     *
     * use PUT here instead of POST to ensure the Idempotency
     */
    @PutMapping("/v1/bfs/employees/update/{id}")
    ResponseEntity<EmployeeGeneralResponse> createEmployee(@PathVariable("id") int id, @RequestBody Employee ee);
    // ----------------------------------------------------------
}
