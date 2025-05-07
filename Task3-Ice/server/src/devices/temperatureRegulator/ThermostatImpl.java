package devices.temperatureRegulator;

import SmartHome.ParamOutOfBounds;
import SmartHome.RoomTemperatureRegulator.State;
import SmartHome.RoomTemperatureRegulator.Temperature;
import SmartHome.RoomTemperatureRegulator.Thermostat;
import com.zeroc.Ice.Current;
import devices.TurnableDevice;

public class ThermostatImpl extends TurnableDevice implements Thermostat {

    private State state;
    private float actualTemperature;
    private float targetTemperature;

    public ThermostatImpl(String name) {
        super(name);
        this.state = State.OFF;
        this.actualTemperature = 17.0F;
        this.targetTemperature = 22.0F;
    }

    private void setState() {
        if (actualTemperature >= targetTemperature - 0.1 && actualTemperature <= targetTemperature + 0.1) {
            state = State.OFF;
        } else if (actualTemperature > targetTemperature) {
            state = State.COOLING;
        } else {
            state = State.HEATING;
        }
    }

    @Override
    public void turnOn(Current current) {
        turnedOn = true;
        setState();
    }

    @Override
    public void turnOff(Current current) {
        turnedOn = false;
        state = State.OFF;
    }


    @Override
    public Temperature getTemperature(Current current) {
        return new Temperature(actualTemperature, targetTemperature);
    }

    @Override
    public void setTargetTemperature(float target, Current current) throws ParamOutOfBounds {
        if (target <= 10.0F || target >= 30.0F) {
            throw new ParamOutOfBounds("Target temperature must be between 10 and 30 degrees");
        }
        this.targetTemperature = target;
        setState();
    }

    @Override
    public State getState(Current current) {
        return state;
    }
}
