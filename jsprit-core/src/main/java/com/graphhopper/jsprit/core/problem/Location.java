/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.jsprit.core.problem;

import com.graphhopper.jsprit.core.util.Coordinate;

/**
 * Created by schroeder on 16.12.14.
 */
public final class Location implements HasIndex, HasId {

    /**
     * Factory method (and shortcut) for creating a location object just with x and y coordinates.
     *
     * @param x coordinate
     * @param y coordinate
     * @return location
     */
    public static Location newInstance(double x, double y) {
        return Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(x, y)).build();
    }

    /**
     * Factory method (and shortcut) for creating location object just with id
     *
     * @param id location id
     * @return location
     */
    public static Location newInstance(String id) {
        return Location.Builder.newInstance().setId(id).build();
    }

    /**
     * Factory method (and shortcut) for creating location object just with location index
     *
     * @param index
     * @return
     */
    public static Location newInstance(int index) {
        return Location.Builder.newInstance().setIndex(index).build();
    }

    public static class Builder {

        private String id;

        private int index = Location.NO_INDEX;

        private Coordinate coordinate;

        private String name = "";

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder setIndex(int index) {
            if (index < 0) throw new IllegalArgumentException("index must be >= 0");
            this.index = index;
            return this;
        }

        public Builder setCoordinate(Coordinate coordinate) {
            this.coordinate = coordinate;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name){
            this.name = name;
            return this;
        }

        public Location build() {
            if (id == null && coordinate == null) {
                if (index == -1) throw new IllegalArgumentException("either id or coordinate or index must be set");
            }
            if (coordinate != null && id == null) {
                this.id = coordinate.toString();
            }
            if (index != -1 && id == null) {
                this.id = Integer.toString(index);
            }
            return new Location(this);
        }

    }

    public final static int NO_INDEX = -1;

    private final int index;

    private final Coordinate coordinate;

    private final String id;

    private final String name;

    private Location(Builder builder) {
        this.index = builder.index;
        this.coordinate = builder.coordinate;
        this.id = builder.id;
        this.name = builder.name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;

        Location location = (Location) o;

        if (index != location.index) return false;
        if (coordinate != null ? !coordinate.equals(location.coordinate) : location.coordinate != null) return false;
        if (id != null ? !id.equals(location.id) : location.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + (coordinate != null ? coordinate.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[id=" + id + "][index=" + index + "][coordinate=" + coordinate + "]";
    }
}
