package com.sqspublisherconsumer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqspublisherconsumer.model.Employee;
import com.sqspublisherconsumer.payload.EmployeeDto;
import com.sqspublisherconsumer.repository.EmployeeRepository;
import com.sqspublisherconsumer.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Value("${cloud.aws.queue.uri}")
    private String queueUrl;

    @Value("${cloud.aws.bucket-name}")
    private String bucket;

    @Override
    public String employeeProducer(EmployeeDto employeeDto) throws IOException {
        String attachment = uploadImageToBucket(employeeDto);
        Employee employee = new Employee();
        employee.setName(employeeDto.getName());
        employee.setEmail(employeeDto.getEmail());
        employee.setAttachment(attachment);
        String emp = convertEmployeeToString(employee);
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(emp)
                .build();
        SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);
        log.info("Send Message Response : {}", sendMessageResponse);
        return "Sent!!!";
    }

    String convertEmployeeToString(Employee employee) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(employee);
    }

    String uploadImageToBucket(EmployeeDto employeeDto) throws IOException {
        String filename = employeeDto.getAttachment().getOriginalFilename();
        File temp = File.createTempFile("temp", null);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .build();
        s3Client.putObject(putObjectRequest, temp.toPath());
        URL url = s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(filename).build());
        return url.toString();
    }

    @SqsListener("${cloud.aws.queue.uri}")
    void consumeEmployee(String value) throws JsonProcessingException {
        Employee employee = convertStringToEmployee(value);
        Employee saveEmployee = employeeRepository.save(employee);
        log.info("Saved : {}", saveEmployee);
    }

    Employee convertStringToEmployee(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Employee.class);
    }
}
