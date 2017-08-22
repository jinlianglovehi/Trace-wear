package com.huami.sensor.parse;

/**
 * Created by jinliang on 17-8-22.
 */

public class SensorMetaModel {


    /**
     * version : 1
     * data_type : 3
     * time_zone : 32
     * start_time : 1503296501
     * spend_time : 54
     * sample_rate : 25
     * device_source : 4
     * product_version : 0
     * device_id : D8:80:3C:07:B4:CA
     * label : sport
     * sensitivity : 0
     */

    private int version;
    private int data_type;
    private int time_zone;
    private int start_time;
    private int spend_time;
    private int sample_rate;
    private int device_source;
    private int product_version;
    private String device_id;
    private String label;
    private int sensitivity;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getData_type() {
        return data_type;
    }

    public void setData_type(int data_type) {
        this.data_type = data_type;
    }

    public int getTime_zone() {
        return time_zone;
    }

    public void setTime_zone(int time_zone) {
        this.time_zone = time_zone;
    }

    public int getStart_time() {
        return start_time;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    public int getSpend_time() {
        return spend_time;
    }

    public void setSpend_time(int spend_time) {
        this.spend_time = spend_time;
    }

    public int getSample_rate() {
        return sample_rate;
    }

    public void setSample_rate(int sample_rate) {
        this.sample_rate = sample_rate;
    }

    public int getDevice_source() {
        return device_source;
    }

    public void setDevice_source(int device_source) {
        this.device_source = device_source;
    }

    public int getProduct_version() {
        return product_version;
    }

    public void setProduct_version(int product_version) {
        this.product_version = product_version;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    @Override
    public String toString() {
        return "SensorMetaModel{" +
                "version=" + version +
                ", data_type=" + data_type +
                ", time_zone=" + time_zone +
                ", start_time=" + start_time +
                ", spend_time=" + spend_time +
                ", sample_rate=" + sample_rate +
                ", device_source=" + device_source +
                ", product_version=" + product_version +
                ", device_id='" + device_id + '\'' +
                ", label='" + label + '\'' +
                ", sensitivity=" + sensitivity +
                '}';
    }
}
