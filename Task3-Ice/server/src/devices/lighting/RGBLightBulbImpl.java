package devices.lighting;

import SmartHome.Lighting.RGBColor;
import SmartHome.Lighting.RGBLightBulb;

import com.zeroc.Ice.Current;

public class RGBLightBulbImpl extends LightBulbImpl implements RGBLightBulb {

    private RGBColor color;

    public RGBLightBulbImpl(String name) {
        super(name);
    }

    @Override
    public RGBColor getColor(Current current) {
        return color;
    }

    @Override
    public void setColor(RGBColor color, Current current) {
        this.color = color;
    }
}
