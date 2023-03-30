package ru.practicum.event;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@AttributeOverrides({
        @AttributeOverride(name = "lon", column = @Column(name = "location_lon")),
        @AttributeOverride(name = "lat", column = @Column(name = "location_lat"))
})
@Getter
@Setter
public class Location {

    private float lon;

    private float lat;
}
