package devices.lighting;

import SmartHome.Lighting.LightBulb;
import SmartHome.ParamOutOfBounds;
import com.zeroc.Ice.Current;
import devices.TurnableDevice;

public class LightBulbImpl extends TurnableDevice implements LightBulb {

    private byte brightness;

    public LightBulbImpl(String name) {
        super(name);
    }

    @Override
    public byte getBrightness(Current current) {
        return brightness;
    }

    @Override
    public void setBrightness(byte brightness, Current current) throws ParamOutOfBounds {
        if (brightness < 0 || brightness > 100) {
            throw new ParamOutOfBounds("Brightness must be between 0 and 100");
        }
        this.brightness = brightness;
    }
}
