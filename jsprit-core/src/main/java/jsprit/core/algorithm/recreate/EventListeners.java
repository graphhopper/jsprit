package jsprit.core.algorithm.recreate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schroeder on 19/05/15.
 */
class EventListeners {

    private List<EventListener> listeners = new ArrayList<EventListener>();

    public EventListeners() {
        listeners.add(new InsertActivityListener());
        listeners.add(new SwitchVehicleListener());
    }

    public void inform(Event event){
        for(EventListener l : listeners){
            l.inform(event);
        }
    }
}
