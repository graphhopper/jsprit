package com.graphhopper.jsprit.core.pando;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelRow;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Shipment {
    @ExcelRow
    private int rowIndex;

    @ExcelCellName("Shipment Id")
    private String Delivery;

    @ExcelCellName("Source")
    private String src;

    @ExcelCellName("Destination")
    private String dest;

    @ExcelCellName("Quantity")
    private Integer quantity;


    @Override
    public String toString() {
        return "InvoiceExcel [rowIndex=" + rowIndex +
            ", Source=" + src +
            ", Destination=" + dest +
            ", Quantity=" + quantity + "]";
    }
}
