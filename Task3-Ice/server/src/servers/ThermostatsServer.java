package servers;

import SmartHome.DevicePrx;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import devices.temperatureRegulator.ThermostatImpl;
import devices.temperatureRegulator.ThermostatWithPowerImpl;

public class ThermostatsServer {

    private final int port;

    public ThermostatsServer(int port) {
        this.port = port;
    }

    public void run() {
        try (Communicator communicator = Util.initialize()) {

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("CamerasServer", "default -p " + port);

            var deviceManager = new devices.DeviceManagerImpl();
            adapter.add(deviceManager, new Identity("DeviceManager", "thermostats"));

            var thermostat1 = new ThermostatImpl("thermostat living room");
            var identity = new Identity("SimpleLivingRoom", "thermostats");
            adapter.add(thermostat1, identity);
            deviceManager.addDevice(identity.name, DevicePrx.checkedCast(adapter.createProxy(identity)));

            var thermostat2 = new ThermostatWithPowerImpl("advanced thermostat bedroom");
            identity = new Identity("AdvancedBedroom", "thermostats");
            adapter.add(thermostat2, identity);
            deviceManager.addDevice(identity.name, DevicePrx.checkedCast(adapter.createProxy(identity)));

            adapter.activate();
            System.out.println("Entering event processing loop in thermostats server...");
            communicator.waitForShutdown();
        }
    }

    public static void main(String[] args) {
        var server = new ThermostatsServer(10002);
        server.run();
    }
}
