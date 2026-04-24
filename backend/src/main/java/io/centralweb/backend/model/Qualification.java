package io.centralweb.backend.model;


import io.centralweb.backend.enums.ExperienceLevel;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "qualifications")
@EqualsAndHashCode
@ToString
public class Qualification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "qualification_id")
    private UUID qualificationId;
    @Column(name = "job_title")
    private String jobTitle;
    private ExperienceLevel experienceLevel;
    @Column(name = "institution")
    private String institution;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "verified")
    private boolean verified = false;
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private Profile profile;

    public Qualification() {
    }

    public Qualification(UUID qualificationId, String jobTitle, ExperienceLevel experienceLevel, String institution, LocalDate startDate, LocalDate endDate, boolean verified, Profile profile) {
        this.qualificationId = qualificationId;
        this.jobTitle = jobTitle;
        this.experienceLevel = experienceLevel;
        this.institution = institution;
        this.startDate = startDate;
        this.endDate = endDate;
        this.verified = verified;
        this.profile = profile;
    }

    public UUID getQualificationId() {
        return qualificationId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}

