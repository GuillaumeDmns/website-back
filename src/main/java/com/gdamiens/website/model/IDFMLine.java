package com.gdamiens.website.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity(name = "IDFMLine")
@Table(schema = "public", name = "idfm_line")
public class IDFMLine implements Serializable {

    @Id
    private String id;

    public IDFMLine() {
    }

    public IDFMLine(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "IDFMLine{" +
            "id=" + id +
            '}';
    }
}
