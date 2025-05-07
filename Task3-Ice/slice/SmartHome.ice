// slice2py --output-dir client slice/SmartHome.ice
// slice2java --output-dir server/generated slice/SmartHome.ice

module SmartHome {

    interface Device {
        string getName();
        void setName(string name);
        bool isTurnedOn();
        void turnOn();
        void turnOff();
        string getDeviceInfo();
    };

    sequence<string> deviceList;

    exception DeviceNotFound {};

    interface DeviceManager {
        deviceList listDevices();
        Device* getDevice(string id) throws DeviceNotFound;
    };

    exception ParamOutOfBounds {
        string message;
    };

    module Lighting {
        interface LightBulb extends Device {
            byte getBrightness();
            void setBrightness(byte brightness) throws ParamOutOfBounds; // 1 - 100
        };

        struct RGBColor {
            byte red;    // 0 - 255
            byte green;  // 0 - 255
            byte blue;   // 0 - 255
        };

        interface RGBLightBulb extends LightBulb {
            RGBColor getColor();
            void setColor(RGBColor color); // limited by byte type (no exception required)
        };
    };

    module RoomTemperatureRegulator {

        enum State { OFF, HEATING, COOLING };

        struct Temperature {
            float actual;
            float target;
        };

        interface Thermostat extends Device {
            Temperature getTemperature();
            void setTargetTemperature(float target) throws ParamOutOfBounds;
            State getState();
        };

        interface ThermostatWithPowerSetting extends Thermostat {
            byte getPowerSetting();
            void setPowerSetting(byte power) throws ParamOutOfBounds;
        };
    };

    module CameraSystem {

        struct DetectionBox {
            int x;
            int y;
            int width;
            int height;
        };

        struct DetectionMoment {
            long timestamp;
            DetectionBox box;
        };

        sequence<DetectionMoment> Detections;

        enum CameraMode { AUTO, MANUAL, NIGHT };

        interface Camera extends Device {
            Detections getPeopleDetectionMoments(int count) throws ParamOutOfBounds;
            CameraMode getMode();
            void setMode(CameraMode mode);
        };

        struct CameraPosition {
            int pan; // -180 to 180
            int tilt; // -90 to 90
        };

        interface MovableCamera extends Camera {
            CameraPosition getPosition();
            void setPosition(CameraPosition position) throws ParamOutOfBounds;
        };
    };
};