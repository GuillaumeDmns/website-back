package com.gdamiens.website.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity(name = "RATPHistory")
@Table(schema = "public", name = "ratp_history")
public class RATPHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "description")
    private String description;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "operation_date", nullable = false, updatable = false)
    private Timestamp operationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private User user;

    public RATPHistory() {}

    public RATPHistory(String description, Integer userId) {
        this.description = description;
        this.userId = userId;
        this.operationDate = new Timestamp(System.currentTimeMillis());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Timestamp operationDate) {
        this.operationDate = operationDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "RATPHistory{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", user=" + user +
                '}';
    }
}
