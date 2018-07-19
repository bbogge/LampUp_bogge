package com.polito.did2017.lampup.utilities;

/**
 * Created by matil on 23/11/2017.
 */

public class Lamp {
    private String lamp_name;
    private int lamp_image_ID;
    private boolean lamp_state; //true=ON; false=OFF
    private String lamp_IP; //indirizzo IP della lampada
    private int brightness;
    private float hue;
    private float saturation;
    private int mainAngle;
    private int secondaryAngle;

    public Lamp(String lampURL, String lampName, int lampImageID) {
        this.lamp_IP = lampURL;
        this.lamp_name = lampName;
        this.lamp_image_ID = lampImageID;
    }

    public String getLampName() {
        return lamp_name;
    }

    public void setLampName(String newName) {
        lamp_name = newName;
    }

    public String getLampIP() {
        return lamp_IP;
    }

    public int getLampImage() {
        return lamp_image_ID;
    }

    public void turnOn() { lamp_state = true; }

    public void turnOff() { lamp_state = false; }

    public void switchState() { lamp_state = !lamp_state; }

    public boolean	isOn() { return lamp_state; }

    public void setState(boolean newState) {
        lamp_state = newState;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public float getHue() { return hue; }

    public void setHue(float hue) { this.hue = hue; }

    public float getSaturation() { return saturation; }

    public void setSaturation(float saturation) { this.saturation = saturation; }

    public void setHueSat(float hue, float saturation) { this.hue = hue; this.saturation = saturation; }

    public int getMainAngle() {
        return mainAngle;
    }

    public void setMainAngle(int mainAngle) {
        this.mainAngle = mainAngle;
    }

    public int getSecondaryAngle() {
        return secondaryAngle;
    }

    public void setSecondaryAngle(int secondaryAngle) {
        this.secondaryAngle = secondaryAngle;
    }
}
