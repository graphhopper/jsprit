/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.problem;

import jsprit.core.util.Coordinate;

/**
 * Created by schroeder on 16.12.14.
 */
public final class Location implements HasIndex, HasId{

    /**
     * Factory method (and shortcut) for creating a location object just with x and y coordinates.
     *
     * @param x coordinate
     * @param y coordinate
     * @return location
     */
    public static Location newInstance(double x, double y){
        return Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(x,y)).build();
    }

    /**
     * Factory method (and shortcut) for creating location object just with id
     *
     * @param id location id
     * @return location
     */
    public static Location newInstance(String id){
        return Location.Builder.newInstance().setId(id).build();
    }

    /**
     * Factory method (and shortcut) for creating location object just with location index
     *
     * @param index
     * @return
     */
    public static Location newInstance(int index){
        return Location.Builder.newInstance().setIndex(index).build();
    }

    public static class Builder {

        private String id;

        private int index = Location.NO_INDEX;

        private Coordinate coordinate;

        public static Builder newInstance(){ return new Builder(); }

        public Builder setIndex(int index){
            if(index < 0) throw new IllegalArgumentException("index must be >= 0");
            this.index = index;
            return this;
        }

        public Builder setCoordinate(Coordinate coordinate){
            this.coordinate = coordinate;
            return this;
        }

        public Builder setId(String id){
            this.id = id;
            return this;
        }

        public Location build(){
            if(id == null && coordinate == null){
                if(index == -1) throw new IllegalStateException("either id or coordinate or index must be set");
            }
            if(coordinate != null && id == null){
                this.id = coordinate.toString();
            }
            if(index != -1 && id == null){
                this.id = Integer.toString(index);
            }
            return new Location(this);
        }

    }

    public final static int NO_INDEX = -1;

    private final int index;

    private final Coordinate coordinate;

    private final String id;

    private Location(Builder builder) {
        this.index = builder.index;
        this.coordinate = builder.coordinate;
        this.id = builder.id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public Coordinate getCoordinate(){
        return coordinate;
    }

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
        return "[id="+id+"][index="+index+"][coordinate="+coordinate+"]";
    }
}
