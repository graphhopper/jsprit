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

    public static class Builder {

        private String id;

        private int index = -1;

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
}
