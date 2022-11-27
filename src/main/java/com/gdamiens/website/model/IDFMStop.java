package com.gdamiens.website.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity(name = "IDFMStop")
@Table(schema = "public", name = "idfm_stop")
public class IDFMStop implements Serializable {

    @Id
    private Integer id;

    public IDFMStop() {
    }

    public IDFMStop(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "IDFMStop{" +
            "id=" + id +
            '}';
    }
}
