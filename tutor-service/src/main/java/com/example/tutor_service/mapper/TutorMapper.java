package com.example.tutor_service.mapper;

import com.example.tutor_service.dto.request.SearchTutorRequest;
import com.example.tutor_service.dto.response.TutorResponse;
import com.example.tutor_service.entity.Tutor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TutorMapper {

    TutorResponse toTutorResponse(Tutor tutor);

    Tutor toTutor(SearchTutorRequest request);
}
