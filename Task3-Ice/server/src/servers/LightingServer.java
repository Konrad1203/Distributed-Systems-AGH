package servers;

import SmartHome.DevicePrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import devices.DeviceManagerImpl;
import devices.lighting.LightBulbImpl;
import devices.lighting.RGBLightBulbImpl;

public class LightingServer {

    private final int port;

    public LightingServer(int port) {
        this.port = port;
    }

    public void run() {
        try (Communicator communicator = Util.initialize()) {

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("CamerasServer", "default -p " + port);

            var deviceManager = new DeviceManagerImpl();
            adapter.add(deviceManager, new Identity("DeviceManager", "lighting"));

            var lightBulb1 = new LightBulbImpl("light bulb living room");
            var identity = new Identity("SimpleLivingRoom", "lighting");
            adapter.add(lightBulb1, identity);
            deviceManager.addDevice(identity.name, DevicePrx.checkedCast(adapter.createProxy(identity)));

            var rgbLightBulb = new RGBLightBulbImpl("RGB light bulb bedroom");
            identity = new Identity("RGBBedroom", "lighting");
            adapter.add(rgbLightBulb, identity);
            deviceManager.addDevice(identity.name, DevicePrx.checkedCast(adapter.createProxy(identity)));

            adapter.activate();
            System.out.println("Entering event processing loop in lighting server...");
            communicator.waitForShutdown();
        }
    }

    public static void main(String[] args) {
        var server = new LightingServer(10001);
        server.run();
    }
}
