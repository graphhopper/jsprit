//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.job.*;

public class DefaultTourActivityFactory implements TourActivityFactory {
    public DefaultTourActivityFactory() {
    }

    public AbstractActivity createActivity(Service service) {
        Object act;
        if (service instanceof BreakForMultipleTimeWindows) {
            act = new BreakForMultipleTimeWindowsActivity((BreakForMultipleTimeWindows) service);
        } else if (service instanceof RelativeBreak) {
            act = new RelativeBreakActivity((RelativeBreak)service);
        } else if (service instanceof Pickup) {
            act = new PickupService((Pickup)service);
        } else if (service instanceof Delivery) {
            act = new DeliverService((Delivery)service);
        } else {
            act = new PickupService(service);
        }

        return (AbstractActivity)act;
    }
}
