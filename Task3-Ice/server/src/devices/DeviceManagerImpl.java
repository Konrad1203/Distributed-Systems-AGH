package devices;

import SmartHome.Device;
import SmartHome.DeviceManager;
import SmartHome.DeviceNotFound;
import SmartHome.DevicePrx;
import com.zeroc.Ice.Current;

import java.util.HashMap;
import java.util.Map;

public class DeviceManagerImpl implements DeviceManager {

    private final Map<String, DevicePrx> devices = new HashMap<>();

    public void addDevice(String id, DevicePrx device) {
        devices.put(id, device);
    }

    @Override
    public String[] listDevices(Current current) {
        return devices.keySet().toArray(new String[0]);
    }

    @Override
    public DevicePrx getDevice(String id, Current current) throws DeviceNotFound {
        DevicePrx device = devices.get(id);
        if (device == null) throw new DeviceNotFound();
        return device;
    }
}
