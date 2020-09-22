package com.paypal.bfs.test.employeeserv.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.paypal.bfs.test.employeeserv.api.EmployeeResource;
import com.paypal.bfs.test.employeeserv.api.model.Address;
import com.paypal.bfs.test.employeeserv.api.model.Employee;
import com.paypal.bfs.test.employeeserv.api.model.EmployeeGeneralResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * Implementation class for employee resource.
 */
@EnableAutoConfiguration
@PropertySource("classpath:application.properties")
@RestController
@Resource
public class EmployeeResourceImpl implements EmployeeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeResourceImpl.class);

    private String schemaPath = "./src/main/resources/employee.json";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public ResponseEntity<Employee> employeeGetById(int id) {

        LOGGER.info("request employee resource with id {}", id);
        String sql = "SELECT ID, FIRST_NAME, LAST_NAME, BIRTHDAY FROM EMPLOYEES WHERE ID = ?";
        String sqlAddress = "SELECT LINE1, LINE2, STATE, COUNTRY, ZIP_CODE FROM EMPLOYEES WHERE ID = ?";

        try{
            Employee employee = jdbcTemplate.queryForObject(sql,
                new Object[]{id}, new BeanPropertyRowMapper<>(Employee.class));
            Address address = jdbcTemplate.queryForObject(sqlAddress,
                    new Object[]{id}, new BeanPropertyRowMapper<>(Address.class));
            LOGGER.info("employee: {}", employee);
            LOGGER.info("Employee Resource Fetched Successfully.");
            employee.setAddress(address);
            return new ResponseEntity<>(employee, HttpStatus.OK);
        }
        catch (EmptyResultDataAccessException e) {
            LOGGER.warn("No Record Found for Employee {}", id);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EmployeeGeneralResponse> createEmployee(int id, Employee ee) {
        LOGGER.info("update employee resource with id {}", id);
        LOGGER.info("Request body is {}", ee);
        if(!validateRequest(schemaPath, ee)) {
            LOGGER.warn("bad request body");
            EmployeeGeneralResponse employeeGeneralResponse = createResponseWithStatus("FAILURE",
                    "Bad Request JSON body.");
            return new ResponseEntity<>(employeeGeneralResponse, HttpStatus.BAD_REQUEST);
        }

        //if the id in request body doesn't match with the id in url, return error
        if(id!=ee.getId()) {
            LOGGER.warn("Id in request URI {} doesn't match with the id in request body {}", id, ee.getId());
            EmployeeGeneralResponse employeeGeneralResponse = createResponseWithStatus("FAILURE",
                    "Id in request URI doesn't match with the id in request body.");
            return new ResponseEntity<>(employeeGeneralResponse, HttpStatus.BAD_REQUEST);
        }
        String sql = "INSERT INTO EMPLOYEES " +
                    "VALUES" +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?) ";

        try {
            jdbcTemplate.update(sql, id, ee.getFirstName(), ee.getLastName(), ee.getBirthday(),
                    ee.getAddress().getLine1(), ee.getAddress().getLine2(),
                    ee.getAddress().getState(), ee.getAddress().getCountry(),
                    ee.getAddress().getZipCode());
            EmployeeGeneralResponse employeeGeneralResponse = createResponseWithStatus("SUCCESS",
                    "Operation Executed Successfully.");
            return new ResponseEntity<>(employeeGeneralResponse, HttpStatus.OK);
        }
        catch (Exception e) {
            //e.printStackTrace();
            LOGGER.error(e.getLocalizedMessage());
            EmployeeGeneralResponse employeeGeneralResponse = createResponseWithStatus("FAILURE",
                    "database error happens.");
            return new ResponseEntity<>(employeeGeneralResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private EmployeeGeneralResponse createResponseWithStatus(String status, String message) {
        EmployeeGeneralResponse employeeGeneralResponse = new EmployeeGeneralResponse();
        employeeGeneralResponse.setStatus(status);
        employeeGeneralResponse.setMessage(message);
        return employeeGeneralResponse;
    }

    private boolean validateRequest(String schemaPath, Employee ee) {
        try{
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            JsonNode schemaNode = JsonLoader.fromPath(schemaPath);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode dataNode = JsonLoader.fromString(objectMapper.writeValueAsString(ee));
            JsonSchema schema = factory.getJsonSchema(schemaNode);
            ProcessingReport processingReport = schema.validate(dataNode);
            LOGGER.info("JSON report: {}", processingReport);
            return processingReport.isSuccess();
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

}
