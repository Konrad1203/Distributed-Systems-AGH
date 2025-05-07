package devices.temperatureRegulator;

import SmartHome.ParamOutOfBounds;
import SmartHome.RoomTemperatureRegulator.ThermostatWithPowerSetting;
import com.zeroc.Ice.Current;

public class ThermostatWithPowerImpl extends ThermostatImpl implements ThermostatWithPowerSetting {

    private byte powerLevel;

    public ThermostatWithPowerImpl(String name) {
        super(name);
        this.powerLevel = 3;
    }

    @Override
    public byte getPowerSetting(Current current) {
        return powerLevel;
    }

    @Override
    public void setPowerSetting(byte power, Current current) throws ParamOutOfBounds {
        if (power < 1 || power > 5) {
            throw new ParamOutOfBounds("Power level must be between 1 and 5");
        }
        this.powerLevel = power;
    }
}
