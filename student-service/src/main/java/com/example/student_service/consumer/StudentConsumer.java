package com.example.student_service.consumer;


import com.example.student_service.entity.Student;
import com.example.student_service.event.StudentCreatedEvent;
import com.example.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentConsumer {

    private final StudentRepository studentRepository;

    @KafkaListener(topics = "student-created", groupId = "student-service-group")
    public void consumeStudentCreated(StudentCreatedEvent event) {
        Student student = Student.builder()
                .userId(event.getUserId())
                .grade(event.getGrade())
                .build();
        studentRepository.save(student);
    }
}
