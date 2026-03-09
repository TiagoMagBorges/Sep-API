package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.LessonNoteDTO;
import com.necklogic.sepapi.dto.ProfessorAnalyticsDTO;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;

    public ProfessorAnalyticsDTO getStudentAnalytics(UUID studentId, UUID professorId, LocalDateTime start, LocalDateTime end) {
        Student student = studentRepository.findByIdAndProfessorId(studentId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Lesson> lessons = lessonRepository.findAllByStudentIdAndDateTimeBetweenOrderByDateTimeAsc(studentId, start, end);

        long total = lessons.size();
        long attended = lessons.stream().filter(l -> l.getStatus() == LessonStatus.COMPLETED).count();
        long missed = lessons.stream().filter(l -> l.getStatus() == LessonStatus.CANCELED).count();
        double rate = total > 0 ? (double) attended / total * 100 : 0.0;

        List<LessonNoteDTO> notes = lessons.stream()
                .filter(l -> l.getPrivateNotes() != null && !l.getPrivateNotes().isBlank())
                .map(l -> new LessonNoteDTO(l.getDateTime().toString(), l.getPrivateNotes()))
                .collect(Collectors.toList());

        return new ProfessorAnalyticsDTO(
                student.getId(),
                student.getName(),
                total,
                attended,
                missed,
                rate,
                notes
        );
    }
}