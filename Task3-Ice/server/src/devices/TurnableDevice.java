package devices;

import SmartHome.Device;
import com.zeroc.Ice.Current;

public class TurnableDevice implements Device {

    protected String name = "";
    protected boolean turnedOn = false;

    public TurnableDevice(String name) {
        super();
        this.name = name;
    }

    @Override
    public boolean isTurnedOn(Current current) {
        return turnedOn;
    }

    @Override
    public void turnOn(Current current) {
        turnedOn = true;
    }

    @Override
    public void turnOff(Current current) {
        turnedOn = false;
    }

    @Override
    public String getName(Current current) {
        return name;
    }

    @Override
    public void setName(String name, Current current) {
        this.name = name;
    }

    @Override
    public String getDeviceInfo(Current current) {
        return "ID: " + current.id.category + "/" + current.id.name + " | name: " + name + " | turned on: " + turnedOn;
    }
}
