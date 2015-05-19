package jsprit.core.algorithm.recreate;

/**
 * Created by schroeder on 19/05/15.
 */
class InsertActivityListener implements EventListener {

    @Override
    public void inform(Event event) {
        if(event instanceof InsertActivity){
            InsertActivity insertActivity = (InsertActivity) event;
            if(!insertActivity.getNewVehicle().isReturnToDepot()){
                if(insertActivity.getIndex()>=insertActivity.getVehicleRoute().getActivities().size()){
                    insertActivity.getVehicleRoute().getEnd().setLocation(insertActivity.getActivity().getLocation());
                }
            }
            insertActivity.getVehicleRoute().getTourActivities().addActivity(insertActivity.getIndex(),((InsertActivity) event).getActivity());
        }
    }

}
