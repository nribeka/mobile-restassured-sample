package com.nribeka.search.sample;

import java.util.Date;

public class Observation {

    private String uuid;

    private String patientUuid;

    private Float valueNumeric;

    private Integer valueInt;

    private Date valueDate;

    private String valueText = "";

    private Date observationDate;

    private String fieldName;

    private String fieldUuid;

    private byte dataType;

    private String json;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public Float getValueNumeric() {
        return valueNumeric;
    }

    public void setValueNumeric(Float valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public void setValueDate(Date valueDate) {
        this.valueDate = valueDate;
    }

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public Date getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(Date observationDate) {
        this.observationDate = observationDate;
    }

    public Integer getValueInt() {
        return valueInt;
    }

    public void setValueInt(Integer valueInt) {
        this.valueInt = valueInt;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldUuid() {
        return fieldUuid;
    }

    public void setFieldUuid(final String fieldUuid) {
        this.fieldUuid = fieldUuid;
    }

    public byte getDataType() {
        return dataType;
    }

    public void setDataType(byte dataType) {
        this.dataType = dataType;
    }

    public String getJson() {
        return json;
    }

    public void setJson(final String json) {
        this.json = json;
    }
}
