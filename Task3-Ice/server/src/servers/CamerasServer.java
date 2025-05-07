package servers;

import SmartHome.DevicePrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import devices.cameras.CameraImpl;
import devices.cameras.MovableCameraImpl;

public class CamerasServer {

    private final int port;

    public CamerasServer(int port) {
        this.port = port;
    }

    public void run() {
        try (Communicator communicator = Util.initialize()) {

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("CamerasServer", "default -p " + port);

            var deviceManager = new devices.DeviceManagerImpl();
            adapter.add(deviceManager, new Identity("DeviceManager", "cameras"));

            var camera1 = new MovableCameraImpl("Camera at entrance");
            var identity = new Identity("EntranceCamera", "cameras");
            adapter.add(camera1, identity);
            deviceManager.addDevice(identity.name, DevicePrx.checkedCast(adapter.createProxy(identity)));

            var camera2 = new CameraImpl("Camera on backyard");
            identity = new Identity("BackYardCamera", "cameras");
            adapter.add(camera2, identity);
            deviceManager.addDevice(identity.name, DevicePrx.checkedCast(adapter.createProxy(identity)));

            adapter.activate();
            System.out.println("Entering event processing loop in cameras server...");
            communicator.waitForShutdown();
        }
    }

    public static void main(String[] args) {
        var server = new CamerasServer(10003);
        server.run();
    }
}
