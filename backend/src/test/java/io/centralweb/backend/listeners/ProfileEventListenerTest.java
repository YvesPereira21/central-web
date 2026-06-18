package io.centralweb.backend.listeners;

import io.centralweb.backend.enums.ExperienceLevel;
import io.centralweb.backend.events.*;
import io.centralweb.backend.service.ProfileService;
import io.centralweb.backend.service.QualificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileEventListenerTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private QualificationService qualificationService;

    @InjectMocks
    private ProfileEventListener profileEventListener;

    private UUID profileId;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();
    }

    @Test
    void shouldAdd20PointsWhenArticleCreated() {
        ArticleCreateEvent event = new ArticleCreateEvent(profileId);

        profileEventListener.handleArticleCreatedAndIncreasePoints(event);

        verify(profileService, times(1)).addPoints(profileId, 20L);
    }

    @Test
    void shouldAdd10PointsWhenQuestionCreated() {
        QuestionCreateEvent event = new QuestionCreateEvent(profileId);

        profileEventListener.handleQuestionCreatedAndIncreasePoints(event);

        verify(profileService, times(1)).addPoints(profileId, 10L);
    }

    @Test
    void shouldAddCalculatedPointsWhenQualificationCreated() {
        QualificationCreateEvent event = new QualificationCreateEvent(profileId, ExperienceLevel.SENIOR);
        long expectedPoints = 30L;
        
        when(qualificationService.getExperienceLevelAndReturnPoints(ExperienceLevel.SENIOR)).thenReturn(expectedPoints);

        profileEventListener.handleQualificationCreatedAndIncreasePoints(event);

        verify(qualificationService, times(1)).getExperienceLevelAndReturnPoints(ExperienceLevel.SENIOR);
        verify(profileService, times(1)).addPoints(profileId, expectedPoints);
    }

    @Test
    void shouldAdd50PointsWhenAnswerAccepted() {
        AnswerAcceptedEvent event = new AnswerAcceptedEvent(profileId);

        profileEventListener.handleAnswerAcceptedAndIncreasePoints(event);

        verify(profileService, times(1)).addPoints(profileId, 50L);
    }

    @Test
    void shouldRemove20PointsWhenArticleDeleted() {
        ArticleDeleteEvent event = new ArticleDeleteEvent(profileId);

        profileEventListener.handleArticleDeletedAndDecreasePoints(event);

        verify(profileService, times(1)).addPoints(profileId, -20L);
    }

    @Test
    void shouldRemove10PointsWhenQuestionDeleted() {
        QuestionDeleteEvent event = new QuestionDeleteEvent(profileId);

        profileEventListener.handleQuestionDeletedAndDecreasePoints(event);

        verify(profileService, times(1)).addPoints(profileId, -10L);
    }

    @Test
    void shouldRemoveCalculatedPointsWhenQualificationDeleted() {
        QualificationDeleteEvent event = new QualificationDeleteEvent(profileId, ExperienceLevel.JUNIOR);
        long expectedPoints = 10L;

        when(qualificationService.getExperienceLevelAndReturnPoints(ExperienceLevel.JUNIOR)).thenReturn(expectedPoints);

        profileEventListener.handleQualificationDeletedAndDecreasePoints(event);

        verify(qualificationService, times(1)).getExperienceLevelAndReturnPoints(ExperienceLevel.JUNIOR);
        verify(profileService, times(1)).addPoints(profileId, -expectedPoints);
    }

    @Test
    void shouldRemove50PointsWhenAnswerUnaccepted() {
        AnswerUnacceptedEvent event = new AnswerUnacceptedEvent(profileId);

        profileEventListener.handleAnswerUnacceptedAndDecreasePoints(event);

        verify(profileService, times(1)).addPoints(profileId, -50L);
    }
}
