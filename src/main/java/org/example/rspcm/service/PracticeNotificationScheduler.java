package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rspcm.model.entity.PracticeParticipationMember;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.NotificationType;
import org.example.rspcm.repository.PracticeParticipationMemberRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeNotificationScheduler {

    private final PracticeParticipationMemberRepository memberRepository;
    private final FcmService fcmService;
    private final NotificationService notificationService;

    /**
     * Every day at 19:00 — remind students who haven't written today's logbook entry
     * for their schedulingRequired practices.
     */
    @Scheduled(cron = "0 0 19 * * *")
    public void sendLogbookReminders() {
        LocalDate today = LocalDate.now();
        List<PracticeParticipationMember> members =
                memberRepository.findMembersNeedingLogbookReminderToday(today);

        // Group by user to send one notification even if user is in multiple practices
        Map<Long, List<PracticeParticipationMember>> byUser = members.stream()
                .collect(Collectors.groupingBy(m -> m.getUser().getId()));

        for (Map.Entry<Long, List<PracticeParticipationMember>> entry : byUser.entrySet()) {
            User user = entry.getValue().get(0).getUser();
            List<String> practiceNames = entry.getValue().stream()
                    .map(m -> m.getPracticeParticipation().getExamPractice().getPractice().getName())
                    .distinct()
                    .toList();

            String body = practiceNames.size() == 1
                    ? "Не забудьте записать, что вы сделали сегодня по практике «" + practiceNames.get(0) + "»."
                    : "Не забудьте записать выполненную работу по " + practiceNames.size() + " практикам.";

            String title = "Запишите в дневник практики";
            fcmService.sendToUser(user, title, body);
            try {
                Long referenceId = entry.getValue().get(0).getPracticeParticipation().getId();
                notificationService.create(user, title, body, NotificationType.PRACTICE_REMINDER, referenceId);
            } catch (Exception e) {
                log.warn("Failed to persist logbook reminder notification for user {}", user.getId(), e);
            }
        }

        log.info("Logbook reminders sent to {} students", byUser.size());
    }

    /**
     * Every day at 09:00 — remind students whose practice submission deadline is in 3 days.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDeadlineReminders() {
        LocalDateTime from = LocalDateTime.now().plusDays(2).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime to = LocalDateTime.now().plusDays(4).withHour(0).withMinute(0).withSecond(0);

        List<PracticeParticipationMember> members =
                memberRepository.findMembersWithUpcomingDeadline(from, to);

        Map<Long, List<PracticeParticipationMember>> byUser = members.stream()
                .collect(Collectors.groupingBy(m -> m.getUser().getId()));

        for (Map.Entry<Long, List<PracticeParticipationMember>> entry : byUser.entrySet()) {
            User user = entry.getValue().get(0).getUser();
            List<String> examTitles = entry.getValue().stream()
                    .map(m -> m.getPracticeParticipation().getExam().getTitle())
                    .distinct()
                    .toList();

            String body = examTitles.size() == 1
                    ? "До сдачи практики по экзамену «" + examTitles.get(0) + "» осталось 3 дня."
                    : "Через 3 дня заканчивается срок сдачи по " + examTitles.size() + " экзаменам.";

            String title = "Срок сдачи через 3 дня";
            fcmService.sendToUser(user, title, body);
            try {
                Long referenceId = entry.getValue().get(0).getPracticeParticipation().getId();
                notificationService.create(user, title, body, NotificationType.DEADLINE_REMINDER, referenceId);
            } catch (Exception e) {
                log.warn("Failed to persist deadline reminder notification for user {}", user.getId(), e);
            }
        }

        log.info("Deadline reminders sent to {} students", byUser.size());
    }
}
