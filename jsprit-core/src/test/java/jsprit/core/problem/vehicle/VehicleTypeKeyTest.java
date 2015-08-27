package jsprit.core.problem.vehicle;


import jsprit.core.problem.Location;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VehicleTypeKeyTest {

    @Test
    public void typeIdentifierShouldBeEqual() {
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("start")).addSkill("skill1").addSkill("skill2")
            .addSkill("skill3").build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("start")).addSkill("skill2").addSkill("skill1")
            .addSkill("skill3").build();
        assertTrue(v1.getVehicleTypeIdentifier().equals(v2.getVehicleTypeIdentifier()));
    }

    @Test
    public void typeIdentifierShouldNotBeEqual() {
        Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance("start")).addSkill("skill1").addSkill("skill2")
            .build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("start")).addSkill("skill2").addSkill("skill1")
            .addSkill("skill3").build();
        assertFalse(v1.getVehicleTypeIdentifier().equals(v2.getVehicleTypeIdentifier()));
    }
}
