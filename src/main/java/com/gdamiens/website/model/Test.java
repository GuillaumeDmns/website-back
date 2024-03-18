package com.gdamiens.website.model;

import org.locationtech.jts.geom.Geometry;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity(name = "Test")
@Table(schema = "public", name = "test")
public class Test implements Serializable {
    // identity + int = serial type in postgres. identity + long = bigserial.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // column definition define postgres column type
    @Column(name = "geog", columnDefinition = "geography")
    private Geometry geog;
    // Constructor, getters and setters should be here

    public Test() {
    }

    public Test(Integer id, Geometry geog) {
        this.id = id;
        this.geog = geog;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Geometry getGeog() {
        return geog;
    }

    public void setGeog(Geometry geog) {
        this.geog = geog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Test test = (Test) o;
        return Objects.equals(id, test.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Test{" +
            "id=" + id +
            ", geog=" + geog +
            '}';
    }
}
