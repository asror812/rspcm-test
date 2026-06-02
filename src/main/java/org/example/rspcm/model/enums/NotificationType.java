package org.example.rspcm.model.enums;

public enum NotificationType {
    TEAM_INVITATION,       // student invited to a team
    SUBMISSION_RECEIVED,   // teacher: student submitted work
    SUBMISSION_GRADED,     // student: teacher accepted work
    SUBMISSION_RETURNED,   // student: teacher returned work for revision
    PRACTICE_REMINDER,     // student: logbook reminder
    DEADLINE_REMINDER      // student: exam deadline approaching
}
