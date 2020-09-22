package com.paypal.bfs.test.employeeserv;

import com.paypal.bfs.test.employeeserv.api.model.Address;
import com.paypal.bfs.test.employeeserv.api.model.Employee;
import com.paypal.bfs.test.employeeserv.api.model.EmployeeGeneralResponse;
import com.paypal.bfs.test.employeeserv.impl.EmployeeResourceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeFunctionalTest {
    @Autowired
    private EmployeeResourceImpl employeeResource;

    @Test
    public void testGetFunc() throws Exception {
        ResponseEntity<Employee> entity = employeeResource.employeeGetById(1);
        Employee ee = entity.getBody();
        Assert.assertEquals(entity.getStatusCode().value(), 200);
        Assert.assertEquals(ee.getLastName(), "Chen");
        Assert.assertEquals(ee.getFirstName(), "Carter");
    }

    @Test
    public void testGetNotExistFunc() throws Exception {
        ResponseEntity<Employee> entity = employeeResource.employeeGetById(10);
        Employee ee = entity.getBody();
        Assert.assertEquals(entity.getStatusCode().value(), 404);
    }

    @Test
    public void testCreateFunc() throws Exception {
        Employee employee = createEmployeeRequest(true, 2);
        ResponseEntity<EmployeeGeneralResponse> entity =
                employeeResource.createEmployee(2, employee);
        EmployeeGeneralResponse employeeGeneralResponse = entity.getBody();
        Assert.assertEquals(entity.getStatusCode().value(), 200);
        Assert.assertEquals(employeeGeneralResponse.getStatus(), "SUCCESS");
    }

    @Test
    public void testMissingRequiredField() throws Exception {
        Employee employee = createEmployeeRequest(false, 3);
        ResponseEntity<EmployeeGeneralResponse> entity =
                employeeResource.createEmployee(3, employee);
        EmployeeGeneralResponse employeeGeneralResponse = entity.getBody();
        Assert.assertEquals(entity.getStatusCode().value(), 400);
        Assert.assertEquals(employeeGeneralResponse.getStatus(), "FAILURE");
    }


    // create the employee PUT request body
    // if the argument is false, create a payload that will not pass the validation
    private Employee createEmployeeRequest(boolean includingRequiredFields, int id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setFirstName("aaa");
        employee.setLastName("bbb");
        employee.setBirthday(includingRequiredFields ? "1993-08-07" : null);
        Address address = new Address();
        address.setLine1("1111");
        address.setLine2("2222");
        address.setState("TX");
        address.setCountry("US");
        address.setZipCode("78731");
        employee.setAddress(address);

        return employee;
    }
}
