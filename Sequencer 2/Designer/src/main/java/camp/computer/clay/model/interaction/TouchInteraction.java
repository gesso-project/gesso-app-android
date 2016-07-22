package camp.computer.clay.model.interaction;

import camp.computer.clay.visualization.architecture.Image;
import camp.computer.clay.visualization.util.Point;

public class TouchInteraction {

    public enum Type {

        NONE(0),
        TOUCH(1),
        HOLD(2),
        MOVE(3),
        TWITCH(4),
        DRAG(5),
        RELEASE(6),
        TAP(7),
        PRESS(8);

        // TODO: Change the index to a UUID?
        int index;

        Type(int index) {
            this.index = index;
        }
    }

    public static int MAXIMUM_TOUCH_POINT_COUNT = 5;

    public static int MAXIMUM_TAP_DURATION = 200;

    public static int MINIMUM_HOLD_DURATION = 600;

    public static int MINIMUM_DRAG_DISTANCE = 35;

    final public static long DEFAULT_TIMESTAMP = 0L;

    public Point[] touchPositions = new Point[MAXIMUM_TOUCH_POINT_COUNT];
    public boolean[] isTouching = new boolean[MAXIMUM_TOUCH_POINT_COUNT];

    private Type type;

    private Body body;
    // TODO: targetImage? or is the state of body containing this info (e.g., hand occupied with model <M>)

    // <CONTEXT>
    private long timestamp = DEFAULT_TIMESTAMP;
    // TODO: Link to context, e.g., Sensor data (inc. 3D orienetation, brightness).
    // </CONTEXT>

    public int pointerIndex = -1;

    // touchedImage
    // overlappedImage (not needed, probably, because can look in history, or look at first action in interaction)
    private Image overlappedImage = null;

    public TouchInteraction(Type type) {
        this.type = type;
        this.timestamp = java.lang.System.currentTimeMillis ();

        setup();
    }

    private void setup() {
        for (int i = 0; i < MAXIMUM_TOUCH_POINT_COUNT; i++) {
            touchPositions[i] = new Point(0, 0);
            touchedImage[i] = null;
            isTouching[i] = false;
        }
    }

    public boolean hasTouches () {
        for (int i = 0; i < MAXIMUM_TOUCH_POINT_COUNT; i++) {
            if (isTouching[i]) {
                return true;
            }
        }
        return false;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Body getBody() {
        return this.body;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Point getPosition() {
        return this.touchPositions[0];
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    private Image[] touchedImage = new Image[TouchInteraction.MAXIMUM_TOUCH_POINT_COUNT];

    public boolean isTouching(int fingerIndex) {
        return this.touchedImage[fingerIndex] != null;
    }

    public void setTarget(int fingerIndex, Image image) {
        this.touchedImage[fingerIndex] = image;
    }

    public Image getTarget(int fingerIndex) {
        return this.touchedImage[fingerIndex];
    }

    public boolean isTouching() {
        return isTouching(0);
    }

    public void setTarget(Image image) {
        setTarget(0, image);
        if (image != null) {
            isTouching[0] = true;
        }
    }

    public Image getTarget() {
        return getTarget(0);
    }
}