package com.graphhopper.jsprit.core.pando;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelRow;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Hub {
    @ExcelRow
    private int rowIndex;

    @ExcelCellName("Depot Id")
    private String depotId;

    @ExcelCellName("Lat")
    private double lat;

    @ExcelCellName("Lng")
    private Double lng;

    @ExcelCellName("Name")
    private String name;


    @Override
    public String toString() {
        return "InvoiceExcel [rowIndex=" + rowIndex + ", name=" + name + ", depotId=" + depotId + ", lat=" + lat
            + ", lng=" + lng + "]";
    }
}
