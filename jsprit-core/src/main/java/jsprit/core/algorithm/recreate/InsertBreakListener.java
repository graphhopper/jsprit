package jsprit.core.algorithm.recreate;

import jsprit.core.problem.solution.route.VehicleRoute;

/**
 * Created by schroeder on 19/05/15.
 */
class InsertBreakListener implements EventListener {

    @Override
    public void inform(Event event) {
        if(event instanceof InsertBreak){
            InsertBreak insertActivity = (InsertBreak) event;
            if(!insertActivity.getNewVehicle().isReturnToDepot()){
                if(insertActivity.getIndex()>=insertActivity.getVehicleRoute().getActivities().size()){
                    insertActivity.getVehicleRoute().getEnd().setLocation(insertActivity.getActivity().getLocation());
                }
            }
            VehicleRoute vehicleRoute = ((InsertBreak) event).getVehicleRoute();
            if(!vehicleRoute.isEmpty()){
                if(vehicleRoute.getVehicle() != ((InsertBreak) event).getNewVehicle()){
                    if(vehicleRoute.getVehicle().getBreak() != null){
                        vehicleRoute.getTourActivities().removeJob(vehicleRoute.getVehicle().getBreak());
                    }
                }
            }
            insertActivity.getVehicleRoute().getTourActivities().addActivity(insertActivity.getIndex(),((InsertBreak) event).getActivity());
        }
    }

}
