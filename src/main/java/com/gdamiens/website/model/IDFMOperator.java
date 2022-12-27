package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.OperatorsCSV;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMOperator")
@Table(schema = "public", name = "idfm_operator")
public class IDFMOperator implements Serializable {

    @Id
    private Integer id;

    @Column(name = "name")
    private String name;

    public IDFMOperator() {
    }

    public IDFMOperator(OperatorsCSV operatorsCSV) {
        this.id = operatorsCSV.getOperatorId();
        this.name = operatorsCSV.getOperatorName();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "IDFMOperator{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            '}';
    }
}
