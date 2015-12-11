package jsprit.core.algorithm.recreate;

import jsprit.core.problem.job.Break;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by schroeder on 19/05/15.
 */
class SwitchVehicleListener implements EventListener {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void inform(Event event) {
        if (event instanceof SwitchVehicle) {
            SwitchVehicle switchVehicle = (SwitchVehicle) event;
            if (vehiclesDifferent((SwitchVehicle) event)) {
                logger.trace("switch vehicle ({} to {})",((SwitchVehicle) event).getRoute().getVehicle().getId(),((SwitchVehicle) event).getVehicle().getId());
                Break aBreak = ((SwitchVehicle) event).getRoute().getVehicle().getBreak();
                if (aBreak != null) {
                    boolean removed = ((SwitchVehicle) event).getRoute().getTourActivities().removeJob(aBreak);
                    if (removed) logger.trace("remove {}",aBreak.getId());
                }
            }
            switchVehicle.getRoute().setVehicleAndDepartureTime(switchVehicle.getVehicle(), ((SwitchVehicle) event).getDepartureTime());
        }
    }

    private boolean vehiclesDifferent(SwitchVehicle event) {
        return !event.getRoute().getVehicle().getId().equals(event.getVehicle().getId());
    }
}
