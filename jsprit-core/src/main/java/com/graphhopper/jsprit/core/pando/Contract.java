package com.graphhopper.jsprit.core.pando;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelRow;

public class Contract {
    @ExcelRow
    private int rowIndex;

    @ExcelCellName("Contract Id")
    private String contractId;

    @ExcelCellName("Source")
    private String src;

    @ExcelCellName("Destination")
    private String dest;

    @ExcelCellName("Vehicle Id")
    private String vehicleId;

    @ExcelCellName("Count")
    private Integer count;


    @Override
    public String toString() {
        return "InvoiceExcel [rowIndex=" + rowIndex +
            ", Contract Id=" + contractId +
            ", Vehicle Id=" + vehicleId +
            ", Source=" + src + ", Destination=" + dest
            + ", Count=" + count + "]";
    }
}
