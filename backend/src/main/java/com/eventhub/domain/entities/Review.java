package com.eventhub.domain.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"})
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 1200)
    private String comment;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(length = 1200)
    private String officialReply;

    private Instant repliedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_by_id")
    private AppUser repliedBy;

    @Column(length = 1200)
    private String userFollowUp;

    private Instant userFollowedUpAt;

    public Review() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getOfficialReply() {
        return officialReply;
    }

    public void setOfficialReply(String officialReply) {
        this.officialReply = officialReply;
    }

    public Instant getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(Instant repliedAt) {
        this.repliedAt = repliedAt;
    }

    public AppUser getRepliedBy() {
        return repliedBy;
    }

    public void setRepliedBy(AppUser repliedBy) {
        this.repliedBy = repliedBy;
    }

    public String getUserFollowUp() {
        return userFollowUp;
    }

    public void setUserFollowUp(String userFollowUp) {
        this.userFollowUp = userFollowUp;
    }

    public Instant getUserFollowedUpAt() {
        return userFollowedUpAt;
    }

    public void setUserFollowedUpAt(Instant userFollowedUpAt) {
        this.userFollowedUpAt = userFollowedUpAt;
    }
}
