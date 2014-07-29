package jsprit.core.algorithm.state;

import java.util.Arrays;
import java.util.List;

/**
 * Created by schroeder on 28.07.14.
 */
class StateFactory {

    final static List<String> reservedIds = Arrays.asList("max_load", "load", "costs", "load_at_beginning", "load_at_end", "duration", "latest_operation_start_time", "earliest_operation_start_time"
            , "future_max_load", "past_max_load", "skills");


    static StateId createId(String name){
        if(reservedIds.contains(name)){ throwReservedIdException(name); }
        return new StateIdImpl(name, -1);
    }

    static StateId createId(String name, int index){
        if(reservedIds.contains(name)) throwReservedIdException(name);
        if(index < 10) throwReservedIdException(name);
        return new StateIdImpl(name, index);
    }


    static boolean isReservedId(String stateId){
        return reservedIds.contains(stateId);
    }

    static boolean isReservedId(StateId stateId){
        return reservedIds.contains(stateId.toString());
    }

    static void throwReservedIdException(String name) {
        throw new IllegalStateException("state-id with name '" + name + "' cannot be created. it is already reserved internally.");
    }


    static class StateIdImpl implements StateId {

        private int index;

        public int getIndex(){ return index; }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StateIdImpl other = (StateIdImpl) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

        private String name;

        public StateIdImpl(String name, int index) {
            super();
            this.name = name;
            this.index = index;
        }

        public String toString(){
            return name;
        }
    }
}
