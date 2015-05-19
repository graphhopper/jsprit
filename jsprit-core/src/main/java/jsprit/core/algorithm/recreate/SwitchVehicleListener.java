package jsprit.core.algorithm.recreate;

/**
 * Created by schroeder on 19/05/15.
 */
class SwitchVehicleListener implements EventListener{

    @Override
    public void inform(Event event) {
        if(event instanceof SwitchVehicle){
            SwitchVehicle switchVehicle = (SwitchVehicle) event;
            switchVehicle.getRoute().setVehicleAndDepartureTime(switchVehicle.getVehicle(),((SwitchVehicle) event).getDepartureTime());
        }
    }
}
