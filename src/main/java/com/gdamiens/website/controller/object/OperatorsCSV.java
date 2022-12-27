package com.gdamiens.website.controller.object;

import com.opencsv.bean.CsvBindByPosition;

public class OperatorsCSV {

    @CsvBindByPosition(position = 0)
    private String operatorName;

    @CsvBindByPosition(position = 1)
    private Integer operatorId;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }
}
