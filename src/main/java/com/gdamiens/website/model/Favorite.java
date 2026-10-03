package com.gdamiens.website.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * A user's saved place, stop area or line.
 */
@Entity
@Table(schema = "public", name = "favorite")
public class Favorite {

    public enum Kind { HOME, WORK, PLACE, STOP, LINE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 16)
    private Kind kind;

    @Column(name = "label", length = 200)
    private String label;

    @Column(name = "stop_area_id", length = 64)
    private String stopAreaId;

    @Column(name = "line_id", length = 32)
    private String lineId;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lon")
    private Double lon;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Favorite() {}

    public Favorite(User user, Kind kind) {
        this.user = user;
        this.kind = kind;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Kind getKind() {
        return kind;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStopAreaId() {
        return stopAreaId;
    }

    public void setStopAreaId(String stopAreaId) {
        this.stopAreaId = stopAreaId;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
