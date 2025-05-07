import Ice

import SmartHome

from datetime import datetime, timezone



def print_color(rgb_color):
    print(f"RGB light color: \033[38;2;{rgb_color.red};{rgb_color.green};{rgb_color.blue}m██████\033[0m")


def detection_box_to_string(detection_box):
    return f"x=({detection_box.x}, y={detection_box.y}, width={detection_box.width}, height={detection_box.height})"

def get_date_from_timestamp(timestamp):
    return datetime.fromtimestamp(timestamp, tz=timezone.utc).strftime("%Y-%m-%d %H:%M:%S")


def main():
    with Ice.initialize() as communicator:

        device_dict = dict()

        print("===== Lighting system proxy =====")

        proxy = communicator.stringToProxy("lighting/DeviceManager:default -h localhost -p 10001")
        device_manager = SmartHome.DeviceManagerPrx.checkedCast(proxy)
        if device_manager:
            print("Urządzenia w lighting/DeviceManager:", device_manager.listDevices(), "\n")

            try:
                device = device_manager.getDevice("SimpleLivingRoom")
                light_bulb = SmartHome.Lighting.LightBulbPrx.uncheckedCast(device)
                if light_bulb is None:
                    print("Nieudane rzutowanie na LightBulb")
                    return
                device_dict["lighting/SimpleLivingRoom"] = light_bulb
                light_bulb.turnOn()
                print(light_bulb.getDeviceInfo())
                light_bulb.setBrightness(50)
                print("Current brightness:", light_bulb.getBrightness())
                print("Trying to set brightness over 100%")
                try:
                    light_bulb.setBrightness(150)
                except SmartHome.ParamOutOfBounds as e:
                    print("Error ParamOutOfBounds:", e.message)

            except SmartHome.DeviceNotFound as e:
                print("Nie znaleziono urządzenia SimpleLivingRoom")

            print()

            try:
                device = device_manager.getDevice("RGBBedroom")
                light_bulb = SmartHome.Lighting.RGBLightBulbPrx.uncheckedCast(device)
                if light_bulb is None:
                    print("Nieudane rzutowanie na RGBLightBulb")
                    return
                device_dict["lighting/RGBBedroom"] = light_bulb
                print(light_bulb.getDeviceInfo())
                light_bulb.setBrightness(67)
                print("Current brightness:", light_bulb.getBrightness())
                light_bulb.setColor(SmartHome.Lighting.RGBColor(255, 120, 30))
                print_color(light_bulb.getColor())

            except SmartHome.DeviceNotFound as e:
                print("Nie znaleziono urządzenia RGBBedroom")

            print()

            try:
                print("Trying to get non-existing device...")
                device_manager.getDevice("NON_EXISTING")
            except SmartHome.DeviceNotFound as e:
                print("Nie znaleziono urządzenia NON_EXISTING")

        else:
            print("Invalid proxy")

        print()

        print("===== Thermostats system proxy =====")

        proxy = communicator.stringToProxy("thermostats/DeviceManager:default -h localhost -p 10002")
        device_manager = SmartHome.DeviceManagerPrx.checkedCast(proxy)
        if device_manager:
            print("Urządzenia w thermostats/DeviceManager:", device_manager.listDevices(), "\n")

        proxy = communicator.stringToProxy("thermostats/SimpleLivingRoom:default -h localhost -p 10002")
        thermostat = SmartHome.RoomTemperatureRegulator.ThermostatPrx.checkedCast(proxy)
        if thermostat:
            device_dict["thermostats/SimpleLivingRoom"] = thermostat
            thermostat.turnOn()
            print(thermostat.getDeviceInfo())
            temperature = thermostat.getTemperature()
            print("Current temperature:", temperature.actual, "°C")
            print("Target temperature:", temperature.target, "°C")
            print("State:", thermostat.getState())
            thermostat.setTargetTemperature(15.0)
            print("New target temperature:", thermostat.getTemperature().target, "°C")
            print("New state:", thermostat.getState())
            print("Trying to set target temperature below 5°C")
            try:
                thermostat.setTargetTemperature(4.0)
            except SmartHome.ParamOutOfBounds as e:
                print("Error ParamOutOfBounds:", e.message)
        else:
            print("Invalid proxy")

        print()

        proxy = communicator.stringToProxy("thermostats/AdvancedBedroom:default -h localhost -p 10002")
        thermostat = SmartHome.RoomTemperatureRegulator.ThermostatWithPowerSettingPrx.checkedCast(proxy)
        if thermostat:
            device_dict["thermostats/AdvancedBedroom"] = thermostat
            print(thermostat.getDeviceInfo())
            print("Current power setting:", thermostat.getPowerSetting())
            thermostat.setPowerSetting(5)
            print("New power setting:", thermostat.getPowerSetting())
            print("Trying to set power setting above 10")
            try:
                thermostat.setPowerSetting(11)
            except SmartHome.ParamOutOfBounds as e:
                print("Error ParamOutOfBounds:", e.message)
        else:
            print("Invalid proxy")

        print()

        print("===== Cameras system proxy =====")

        proxy = communicator.stringToProxy("cameras/DeviceManager:default -h localhost -p 10003")
        device_manager = SmartHome.DeviceManagerPrx.checkedCast(proxy)
        if device_manager:
            print("Urządzenia w cameras/DeviceManager:", device_manager.listDevices(), "\n")

        proxy = communicator.stringToProxy("cameras/BackYardCamera:default -h localhost -p 10003")
        camera = SmartHome.CameraSystem.CameraPrx.checkedCast(proxy)
        if camera:
            device_dict["cameras/BackYardCamera"] = camera
            camera.turnOn()
            print(camera.getDeviceInfo())
            print("Camera mode:", camera.getMode())
            camera.setMode(SmartHome.CameraSystem.CameraMode.NIGHT)
            print("New camera mode:", camera.getMode())
            detections = camera.getPeopleDetectionMoments(10)
            print("People detected (last 10):")
            for detection in detections:
                print("Time:", get_date_from_timestamp(detection.timestamp), "Detection box:", detection_box_to_string(detection.box))
            print("Trying to get detection moments with negative count...")
            try:
                camera.getPeopleDetectionMoments(-1)
            except SmartHome.ParamOutOfBounds as e:
                print("Error ParamOutOfBounds:", e.message)
        else:
            print("Invalid proxy")

        print()

        proxy = communicator.stringToProxy("cameras/EntranceCamera:default -h localhost -p 10003")
        camera = SmartHome.CameraSystem.MovableCameraPrx.checkedCast(proxy)
        if camera:
            device_dict["cameras/EntranceCamera"] = camera
            print(camera.getDeviceInfo())
            camera.setPosition(SmartHome.CameraSystem.CameraPosition(pan = 73, tilt = -20))
            position = camera.getPosition()
            print("New camera position:", "pan:", position.pan, "tilt:", position.tilt)
            print("Trying to set camera position with pan out of bounds...")
            try:
                camera.setPosition(SmartHome.CameraSystem.CameraPosition(pan = 200, tilt = 20))
            except SmartHome.ParamOutOfBounds as e:
                print("Error ParamOutOfBounds:", e.message)
        else:
            print("Invalid proxy")

        print()

        print("Interactive part:")
        print("Available devices:", device_dict.keys())
        while True:
            prompt = input("> ")
            if prompt == "exit": break
            fr = prompt.split(" ")
            if len(fr) < 2:
                print("Invalid command")
                continue
            device = device_dict.get(fr[0])
            if device is None:
                print("Invalid device")
                continue
            command = fr[1]
            args = fr[2:]
            try:
                match command:
                    # turnableDevice
                    case "isTurnedOn": print(device.isTurnedOn())
                    case "turnOn": device.turnOn()
                    case "turnOff": device.turnOff()
                    case "getInfo": print(device.getDeviceInfo())
                    case "getName": print(device.getName())
                    case "setName": device.setName(args[0])

                    # lightBulb
                    case "getBrightness": print(device.getBrightness())
                    case "setBrightness": device.setBrightness(int(args[0]))
                    case "getColor": print_color(device.getColor())
                    case "setColor": device.setColor(SmartHome.Lighting.RGBColor(int(args[0]), int(args[1]), int(args[2])))

                    # thermostat
                    case "getTemperature": print(device.getTemperature())
                    case "setTargetTemperature": device.setTargetTemperature(float(args[0]))
                    case "getState": print(device.getState())
                    case "getPowerSetting": print(device.getPowerSetting())
                    case "setPowerSetting": device.setPowerSetting(int(args[0]))

                    # camera
                    case "getMode": print(device.getMode())
                    case "setMode":
                        match (args[0].upper()):
                            case "AUTO": device.setMode(SmartHome.CameraSystem.CameraMode.AUTO)
                            case "MANUAL": device.setMode(SmartHome.CameraSystem.CameraMode.MANUAL)
                            case "NIGHT": device.setMode(SmartHome.CameraSystem.CameraMode.NIGHT)
                            case _: print("Invalid mode")
                    case "getPeopleDetectionMoments": print(device.getPeopleDetectionMoments(int(args[0])))
                    case "getPosition": print(device.getPosition())
                    case "setPosition": device.setPosition(SmartHome.CameraSystem.CameraPosition(int(args[0]), int(args[1])))

                    case _: print("Invalid command")

            except Exception as e:
                print("Invalid command. Error:", e)


if __name__ == "__main__":
    main()