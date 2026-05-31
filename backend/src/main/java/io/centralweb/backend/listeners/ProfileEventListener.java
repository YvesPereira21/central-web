package io.centralweb.backend.listeners;

import io.centralweb.backend.events.ArticleCreateEvent;
import io.centralweb.backend.events.QualificationCreateEvent;
import io.centralweb.backend.events.QuestionCreateEvent;
import io.centralweb.backend.service.ProfileService;
import io.centralweb.backend.service.QualificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProfileEventListener {

    private final ProfileService profileService;
    private final QualificationService qualificationService;

    public ProfileEventListener(ProfileService profileService, QualificationService qualificationService) {
        this.profileService = profileService;
        this.qualificationService = qualificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleArticleCreatedAndIncreasePoints(ArticleCreateEvent event) {
        profileService.addPoints(event.profileId(), 20L);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleQuestionCreatedAndIncreasePoints(QuestionCreateEvent event){
        profileService.addPoints(event.profileId(), 10L);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleQualificationCreatedAndIncreasePoints(QualificationCreateEvent event){
        long pointsToAdd = qualificationService.getExperienceLevelAndReturnPoints(event.experienceLevel());
        profileService.addPoints(event.profileId(), pointsToAdd);
    }
}
