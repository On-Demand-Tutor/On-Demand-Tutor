package com.example.tutor_service.service;

import com.example.tutor_service.entity.Tutor;
import com.example.tutor_service.repository.TutorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;

    public TutorService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    public List<Tutor> getAllTutors() {
        return tutorRepository.findAll();
    }

    public Optional<Tutor> getTutorById(Long id) {
        return tutorRepository.findById(id);
    }

    public Optional<Tutor> getTutorByUserId(Long userId) {
        return tutorRepository.findByUserId(userId);
    }

    public Tutor createTutor(Tutor tutor) {
        return tutorRepository.save(tutor);
    }

    public Tutor updateTutor(Long id, Tutor updatedTutor) {
        return tutorRepository.findById(id)
                .map(t -> {
                    t.setUserId(updatedTutor.getUserId());
                    t.setQualifications(updatedTutor.getQualifications());
                    t.setSkills(updatedTutor.getSkills());
                    t.setRating(updatedTutor.getRating());
                    t.setTeachingGrades(updatedTutor.getTeachingGrades());
                    t.setName(updatedTutor.getName());
                    t.setPrice(updatedTutor.getPrice());
                    t.setAvailableTime(updatedTutor.getAvailableTime());
                    t.setDescription(updatedTutor.getDescription());

                    return tutorRepository.save(t);
                })
                .orElse(null);
    }

    public void deleteTutor(Long id) {
        tutorRepository.deleteById(id);
    }
}
