package com.example.tutor_service.dto.response;

import com.example.tutor_service.entity.Tutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchTutorResponse {
    private List<Tutor> tutors;
    private Long totalElements;
}
