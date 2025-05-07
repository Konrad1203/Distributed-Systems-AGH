package devices.cameras;

import SmartHome.CameraSystem.*;
import SmartHome.ParamOutOfBounds;
import com.zeroc.Ice.Current;
import devices.TurnableDevice;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

public class CameraImpl extends TurnableDevice implements Camera {

    private CameraMode mode;
    private final List<DetectionMoment> detectionMoments;

    public CameraImpl(String name) {
        super(name);
        this.mode = CameraMode.AUTO;
        this.detectionMoments = new LinkedList<>();
        new Thread(() -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    Thread.sleep(getRandom(1, 10) * 1000L);
                    if (detectionMoments.size() >= 100) detectionMoments.removeFirst();
                    detectionMoments.add(
                            new DetectionMoment(
                                    Instant.now().getEpochSecond(),
                                    new DetectionBox(getRandom(0, 1600), getRandom(0, 900), getRandom(0, 200), getRandom(0, 500))
                            )
                    );
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int getRandom(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    @Override
    public DetectionMoment[] getPeopleDetectionMoments(int count, Current current) throws ParamOutOfBounds {
        if (count <= 0) {
            throw new ParamOutOfBounds("Count must be greater than 0");
        }
        int size = detectionMoments.size();
        int fromIndex = Math.max(0, size - count);
        List<DetectionMoment> sublist = detectionMoments.subList(fromIndex, size);
        return sublist.toArray(new DetectionMoment[0]);
    }

    @Override
    public CameraMode getMode(Current current) {
        return mode;
    }

    @Override
    public void setMode(CameraMode mode, Current current) {
        this.mode = mode;
    }
}
