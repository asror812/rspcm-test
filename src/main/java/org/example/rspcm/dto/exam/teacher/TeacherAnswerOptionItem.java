package org.example.rspcm.dto.exam.teacher;

public record TeacherAnswerOptionItem(
        Long id,
        String text,
        boolean correct,
        int orderIndex
) {
}
