package vn.xuandat.Warehouse_management.entity;

public class ExportStatisticDTO {
    private final String label;
    private final long totalQuantity;

    public ExportStatisticDTO(String label, long totalQuantity) {
        this.label = label;
        this.totalQuantity = totalQuantity;
    }

    public String getLabel() {
        return label;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }
}
