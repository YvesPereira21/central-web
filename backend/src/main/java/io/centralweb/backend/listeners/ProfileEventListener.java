package io.centralweb.backend.listeners;

import io.centralweb.backend.events.ArticleCreateEvent;
import io.centralweb.backend.events.QuestionCreateEvent;
import io.centralweb.backend.service.ProfileService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProfileEventListener {

    private final ProfileService profileService;

    public ProfileEventListener(ProfileService profileService) {
        this.profileService = profileService;
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
}
