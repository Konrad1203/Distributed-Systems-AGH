package devices.cameras;

import SmartHome.CameraSystem.CameraPosition;
import SmartHome.CameraSystem.MovableCamera;
import SmartHome.ParamOutOfBounds;
import com.zeroc.Ice.Current;

public class MovableCameraImpl extends CameraImpl implements MovableCamera {

    private int pan;
    private int tilt;

    public MovableCameraImpl(String name) {
        super(name);
        this.pan = 0;
        this.tilt = 0;
    }

    @Override
    public CameraPosition getPosition(Current current) {
        return new CameraPosition(this.pan, this.tilt);
    }

    @Override
    public void setPosition(CameraPosition position, Current current) throws ParamOutOfBounds {
        if (position.pan < -180 || position.pan > 180 || position.tilt < -90 || position.tilt > 90) {
            throw new ParamOutOfBounds("Camera position out of bounds");
        }
        this.pan = position.pan;
        this.tilt = position.tilt;
    }
}
