package com.example.student_service.consumer;


import com.example.student_service.dto.response.SearchTutorResponse;
import com.example.student_service.entity.Student;
import com.example.student_service.event.StudentCreatedEvent;
import com.example.student_service.event.StudentUpdatedEvent;
import com.example.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentConsumer {

    private final StudentRepository studentRepository;

    @KafkaListener(topics = "student-created", groupId = "student-service-group",containerFactory = "kafkaListenerContainerFactoryForcreateStudent")
    public void consumeStudentCreated(StudentCreatedEvent event) {
        System.out.println("Student Nhận được event từ Kafka rồi nhé ok ok ++++>>>: " + event);
        Student student = Student.builder()
                .userId(event.getUserId())
                .grade(event.getGrade())
                .build();
        studentRepository.save(student);
    }

    @KafkaListener(topics = "student-updated", groupId = "student-service-group", containerFactory = "kafkaListenerContainerFactoryForUpdateStudent")
    public void consumeStudentUpdated(StudentUpdatedEvent event) {
        System.out.println("Student Nhận được event update từ Kafka rồi nhé ok ok ++++>>>: " + event);

        Student student = studentRepository.findByUserId(event.getUserId())
                .orElseThrow(() -> new RuntimeException("Student not found with userId: " + event.getUserId()));

        if (event.getGrade() != null) {
            student.setGrade(event.getGrade());
        }
        studentRepository.save(student);
    }

    @KafkaListener(topics = "search-tutor-response", groupId = "student-service-group", containerFactory = "kafkaListenerContainerFactoryForSearchTutor")
    public void consumeSearchTutor(SearchTutorResponse event) {
        System.out.println("Student Nhận được event search từ Kafka rồi nhé ok ok ++++>>>: " + event);
        if (event != null && event.getTutors() != null) {
            System.out.println("Số lượng tutor tìm được: " + event.getTutors().size());
            System.out.println("Tổng số tutor trong DB: " + event.getTotalElements());

            event.getTutors().forEach(tutor -> {
                System.out.println("Tutor ID: " + tutor.getUserId() +
                        ", Skills: " + tutor.getSkills() +
                        ", Rating: " + tutor.getRating());
            });
        } else {
            System.out.println("Không tìm thấy tutor nào!");
        }

    }


}
