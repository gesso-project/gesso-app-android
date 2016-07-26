package camp.computer.clay.viz.img;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

import camp.computer.clay.app.Application;
import camp.computer.clay.app.VizSurface;
import camp.computer.clay.model.interaction.OnTouchActionListener;
import camp.computer.clay.model.sim.Frame;
import camp.computer.clay.model.sim.Path;
import camp.computer.clay.model.sim.Port;
import camp.computer.clay.model.interaction.TouchInteraction;
import camp.computer.clay.viz.arch.Image;
import camp.computer.clay.viz.arch.Visibility;
import camp.computer.clay.viz.arch.Viz;
import camp.computer.clay.viz.util.Geometry;
import camp.computer.clay.viz.util.Point;
import camp.computer.clay.viz.util.Rectangle;

public class FrameImage extends Image {

    public final static String TYPE = "base";

    // TODO: Replace these with dynamic counts.
    final static int PORT_GROUP_COUNT = 4;
    final static int PORT_COUNT = 12;

    // <STYLE>
    // TODO: Make these private once the map is working well and the sprite is working well.
    public double boardHeight = 250.0f;
    public double boardWidth = 250.0f;
    public Rectangle shape = new Rectangle(boardWidth, boardHeight);

    private String boardColorString = "f7f7f7"; // "404040"; // "414141";
    private int boardColor = Color.parseColor("#ff" + boardColorString); // Color.parseColor("#212121");
    private boolean showBoardOutline = true;
    private String boardOutlineColorString = "414141";
    private int boardOutlineColor = Color.parseColor("#ff" + boardOutlineColorString); // Color.parseColor("#737272");
    private double boardOutlineThickness = 3.0f;

    private double targetTransparency = 1.0f;
    private double currentTransparency = targetTransparency;

    private double portGroupWidth = 50;
    private double portGroupHeight = 13;
    private String portGroupColorString = "3b3b3b";
    private int portGroupColor = Color.parseColor("#ff" + portGroupColorString);
    private boolean showPortGroupOutline = false;
    private String portGroupOutlineColorString = "000000";
    private int portGroupOutlineColor = Color.parseColor("#ff" + portGroupOutlineColorString);
    private double portGroupOutlineThickness = boardOutlineThickness;

    private double distanceLightsToEdge = 12.0f;
    private double lightWidth = 12;
    private double lightHeight = 20;
    private boolean showLightOutline = true;
    private double lightOutlineThickness = 1.0f;
    private int lightOutlineColor = Color.parseColor("#e7e7e7");
    // </STYLE>

    public FrameImage(Frame frame) {
        super(frame);
        setType(TYPE);
        setup();
    }

    private void setup() {
        setupStyle();
//        setupPortImages();
    }

    private void setupStyle() {
    }

    public void setupPortImages() {

        // Add a port sprite for each of the associated base's ports
        for (Port port : getFrame().getPorts()) {
            PortImage portImage = new PortImage(port);
            portImage.setViz(getViz());
            getViz().addImage(portImage, "ports");
        }
    }

    public Frame getFrame() {
        return (Frame) getModel();
    }

    public ArrayList<PortImage> getPortImages() {
        ArrayList<PortImage> portImages = new ArrayList<PortImage>();
        Frame frame = getFrame();

        for (Port port : frame.getPorts()) {
            PortImage portImage = (PortImage) getViz().getImage(port);
            portImages.add(portImage);
        }

        return portImages;
    }

    public PortImage getPortImage(int index) {
        Frame frame = getFrame();
        PortImage portImage = (PortImage) getViz().getImage(frame.getPort(index));
        return portImage;
    }

    // TODO: Remove this! Store Port index/id
    public int getPortImageIndex(PortImage portImage) {
        Port port = (Port) getViz().getModel(portImage);
        if (getFrame().getPorts().contains(port)) {
            return this.getFrame().getPorts().indexOf(port);
        }
        return -1;
    }

    public void generate() {
        updateLightImages();
        updatePortGroupImages();
    }

    public void draw(VizSurface vizSurface) {

        if (isVisible()) {
            // drawPortPeripheralImages(visualizationSurface);
            drawPortGroupImages(vizSurface);
            drawBoardImage(vizSurface);
            drawLightImages(vizSurface);

            if (Application.ENABLE_GEOMETRY_ANNOTATIONS) {
                vizSurface.getPaint().setColor(Color.GREEN);
                vizSurface.getPaint().setStyle(Paint.Style.STROKE);
                getViz().getPalette().drawCircle(getPosition(), shape.getWidth(), 0);
                getViz().getPalette().drawCircle(getPosition(), shape.getWidth() / 2.0f, 0);
            }
        }
    }

    public Rectangle getShape() {
        return this.shape;
    }

    public void drawBoardImage(VizSurface vizSurface) {

        Canvas canvas = vizSurface.getCanvas();
        Paint paint = vizSurface.getPaint();

        // Color
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(this.boardColor);
        getViz().getPalette().drawRectangle(getPosition(), getRotation(), shape.getWidth(), shape.getHeight());

        // Outline
        if (this.showBoardOutline) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(this.boardOutlineColor);
            paint.setStrokeWidth((float) boardOutlineThickness);
            getViz().getPalette().drawRectangle(getPosition(), getRotation(), shape.getWidth(), shape.getHeight());
        }
    }

    Point[] portGroupCenterPositions = new Point[PORT_GROUP_COUNT];

    public void updatePortGroupImages() {
        // <SHAPE>
        // Positions before rotation
        portGroupCenterPositions[0] = new Point(
                getPosition().getX() + 0,
                getPosition().getY() + ((shape.getHeight() / 2.0f) + (portGroupHeight / 2.0f))
        );
        portGroupCenterPositions[1] = new Point(
                getPosition().getX() + ((shape.getWidth() / 2.0f) + (portGroupHeight / 2.0f)),
                getPosition().getY() + 0
        );
        portGroupCenterPositions[2] = new Point(
                getPosition().getX() + 0,
                getPosition().getY() - ((shape.getHeight() / 2.0f) + (portGroupHeight / 2.0f))
        );
        portGroupCenterPositions[3] = new Point(
                getPosition().getX() - ((shape.getWidth() / 2.0f) + (portGroupHeight / 2.0f)),
                getPosition().getY() + 0
        );
        // </SHAPE>

        for (int i = 0; i < PORT_GROUP_COUNT; i++) {

            // Calculate rotated position
            portGroupCenterPositions[i] = Geometry.calculateRotatedPoint(getPosition(), getRotation() + (((i - 1) * 90) - 90) + ((i - 1) * 90), portGroupCenterPositions[i]);
        }
    }

    public void drawPortGroupImages(VizSurface vizSurface) {

        Canvas canvas = vizSurface.getCanvas();
        Paint paint = vizSurface.getPaint();

        for (int i = 0; i < PORT_GROUP_COUNT; i++) {

            // Color
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(this.portGroupColor);
            getViz().getPalette().drawRectangle(portGroupCenterPositions[i], getRotation() + ((i * 90) + 90), portGroupWidth, portGroupHeight);

            // Outline
            if (this.showPortGroupOutline) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth((float) portGroupOutlineThickness);
                paint.setColor(this.portGroupOutlineColor);
                getViz().getPalette().drawRectangle(portGroupCenterPositions[i], getRotation(), portGroupWidth, portGroupHeight);
            }

        }
    }

    public void drawPortPeripheralImages(VizSurface vizSurface) {

        Canvas canvas = vizSurface.getCanvas();
        Paint paint = vizSurface.getPaint();

        // <SHAPE>
        Point[] portGroupCenterPositions = new Point[PORT_GROUP_COUNT];

        // Positions before rotation
        portGroupCenterPositions[0] = new Point(
                getPosition().getX() + 0,
                getPosition().getY() + ((shape.getHeight()) + (portGroupHeight / 2.0f))
        );
        portGroupCenterPositions[1] = new Point(
                getPosition().getX() + ((shape.getWidth()) + (portGroupHeight / 2.0f)),
                getPosition().getY() + 0
        );
        portGroupCenterPositions[2] = new Point(
                getPosition().getX() + 0,
                getPosition().getY() - ((shape.getHeight()) + (portGroupHeight / 2.0f))
        );
        portGroupCenterPositions[3] = new Point(
                getPosition().getX() - ((shape.getWidth()) + (portGroupHeight / 2.0f)),
                getPosition().getY() + 0
        );
        // </SHAPE>

        for (int i = 0; i < PORT_GROUP_COUNT; i++) {

            // Calculate rotated position
            portGroupCenterPositions[i] = Geometry.calculateRotatedPoint(getPosition(), getRotation() + (((i - 1) * 90) - 90) + ((i - 1) * 90), portGroupCenterPositions[i]);

            // Color
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(this.portGroupColor);
            getViz().getPalette().drawCircle(portGroupCenterPositions[i], 20, 0);

            // Outline
            if (this.showPortGroupOutline) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth((float) portGroupOutlineThickness);
                paint.setColor(this.portGroupOutlineColor);
                getViz().getPalette().drawCircle(portGroupCenterPositions[i], 20, 0);
            }

        }
    }

    Point[] lightCenterPositions = new Point[PORT_COUNT];
    double[] lightRotationAngle = new double[12];

    private void updateLightImages() {
        // <SHAPE>
        lightCenterPositions[0] = new Point(
                getPosition().getX() + (-20),
                getPosition().getY() + ((shape.getHeight() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f))
        );
        lightCenterPositions[1] = new Point(
                getPosition().getX() + (0),
                getPosition().getY() + ((shape.getHeight() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f))
        );
        lightCenterPositions[2] = new Point(
                getPosition().getX() + (+20),
                getPosition().getY() + ((shape.getHeight() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f))
        );

        lightCenterPositions[3] = new Point(
                getPosition().getX() + ((shape.getWidth() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f)),
                getPosition().getY() + (+20)
        );
        lightCenterPositions[4] = new Point(
                getPosition().getX() + ((shape.getWidth() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f)),
                getPosition().getY() + (0)
        );
        lightCenterPositions[5] = new Point(
                getPosition().getX() + ((shape.getWidth() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f)),
                getPosition().getY() + (-20)
        );

        lightCenterPositions[6] = new Point(
                getPosition().getX() + (+20),
                getPosition().getY() - ((shape.getHeight() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f))
        );
        lightCenterPositions[7] = new Point(
                getPosition().getX() + (0),
                getPosition().getY() - ((shape.getHeight() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f))
        );
        lightCenterPositions[8] = new Point(
                getPosition().getX() + (-20),
                getPosition().getY() - ((shape.getHeight() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f))
        );

        lightCenterPositions[9] = new Point(
                getPosition().getX() - ((shape.getWidth() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f)),
                getPosition().getY() + (-20)
        );
        lightCenterPositions[10] = new Point(
                getPosition().getX() - ((shape.getWidth() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f)),
                getPosition().getY() + (0)
        );
        lightCenterPositions[11] = new Point(
                getPosition().getX() - ((shape.getWidth() / 2.0f) + (-distanceLightsToEdge) + -(lightHeight / 2.0f)),
                getPosition().getY() + (+20)
        );

        lightRotationAngle[0] = 0;
        lightRotationAngle[1] = 0;
        lightRotationAngle[2] = 0;
        lightRotationAngle[3] = 90;
        lightRotationAngle[4] = 90;
        lightRotationAngle[5] = 90;
        lightRotationAngle[6] = 180;
        lightRotationAngle[7] = 180;
        lightRotationAngle[8] = 180;
        lightRotationAngle[9] = 270;
        lightRotationAngle[10] = 270;
        lightRotationAngle[11] = 270;
        // </SHAPE>

        // Calculate rotated position
        for (int i = 0; i < PORT_COUNT; i++) {
            lightCenterPositions[i] = Geometry.calculateRotatedPoint(getPosition(), getRotation(), lightCenterPositions[i]);
        }
    }

    public void drawLightImages(VizSurface vizSurface) {

        Canvas canvas = vizSurface.getCanvas();
        Paint paint = vizSurface.getPaint();

        for (int i = 0; i < PORT_COUNT; i++) {

            // Color
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(3);
            Port port = (Port) getFrame().getPort(i);
            if (port.getType() != Port.Type.NONE) {
                paint.setColor(camp.computer.clay.viz.util.Color.setTransparency(this.getPortImage(i).getUniqueColor(), (float) currentTransparency));
            } else {
                paint.setColor(camp.computer.clay.viz.util.Color.setTransparency(PortImage.FLOW_PATH_COLOR_NONE, (float) currentTransparency));
            }
            getViz().getPalette().drawRectangle(lightCenterPositions[i], getRotation() + lightRotationAngle[i], lightWidth, lightHeight);

            // Outline
            if (this.showLightOutline) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth((float) lightOutlineThickness);
                paint.setColor(this.lightOutlineColor);
                getViz().getPalette().drawRectangle(lightCenterPositions[i], getRotation() + lightRotationAngle[i], lightWidth, lightHeight);
            }
        }
    }

    // TODO: Move this into Image (send to all Images)
    public void setTransparency(final double transparency) {

        targetTransparency = transparency;

        currentTransparency = targetTransparency;
        String transparencyString = String.format("%02x", (int) currentTransparency * 255);

        // Frame color
        boardColor = Color.parseColor("#" + transparencyString + boardColorString);
        boardOutlineColor = Color.parseColor("#" + transparencyString + boardOutlineColorString);

        // Header color
        portGroupColor = Color.parseColor("#" + transparencyString + portGroupColorString);
        portGroupOutlineColor = Color.parseColor("#" + transparencyString + portGroupOutlineColorString);

        // TODO: Replace this with manual translation through colorspace.
//        if (this.targetTransparency != transparency) {
//
//            Animation.scaleValue(255.0f * targetTransparency, 255.0f * transparency, 200, new Animation.OnScaleListener() {
//                @Override
//                public void onScale(double currentScale) {
//                    currentTransparency = currentScale / 255.0f;
//                    String transparencyString = String.format("%02x", (int) currentScale);
//
//                    // Frame color
//                    boardColor = Color.parseColor("#" + transparencyString + boardColorString);
//                    boardOutlineColor = Color.parseColor("#" + transparencyString + boardOutlineColorString);
//
//                    // Header color
//                    portGroupColor = Color.parseColor("#" + transparencyString + portGroupColorString);
//                    portGroupOutlineColor = Color.parseColor("#" + transparencyString + portGroupOutlineColorString);
//                }
//            });
//
//            this.targetTransparency = transparency;
//        }
    }

    public void showPortImages() {
        for (PortImage portImage : getPortImages()) {
            portImage.setVisibility(Visibility.VISIBLE);
            portImage.showDocks();
        }
    }

    public void hidePortImages() {
        for (PortImage portImage : getPortImages()) {
            portImage.setVisibility(Visibility.INVISIBLE);
        }
    }

    public void showPathImages() {
        for (PortImage portImage : getPortImages()) {
            portImage.setPathVisibility(Visibility.VISIBLE);
        }
    }

    public void hidePathImages() {
        for (PortImage portImage : getPortImages()) {
            portImage.setPathVisibility(Visibility.INVISIBLE);
            portImage.showDocks();
        }
    }

    //-------------------------
    // Interaction
    //-------------------------

    public boolean isTouching(Point point) {
        if (isVisible()) {
            return Geometry.calculateDistance(getPosition(), point) < (this.shape.getHeight() / 2.0f);
        } else {
            return false;
        }
    }

    public boolean isTouching(Point point, double padding) {
        if (isVisible()) {
            return Geometry.calculateDistance(getPosition(), point) < (this.shape.getHeight() / 2.0f + padding);
        } else {
            return false;
        }
    }

    @Override
    public void onTouchInteraction(TouchInteraction touchInteraction) {

        if (touchInteraction.getType() == OnTouchActionListener.Type.NONE) {
            // Log.v("onTouchInteraction", "TouchInteraction.NONE to " + CLASS_NAME);
        } else if (touchInteraction.getType() == OnTouchActionListener.Type.TOUCH) {
            // Log.v("onTouchInteraction", "TouchInteraction.TOUCH to " + CLASS_NAME);
        } else if (touchInteraction.getType() == OnTouchActionListener.Type.TAP) {

            // Focus on touched form
            showPortImages();
            showPathImages();
            setTransparency(1.0f);

            // TODO: Speak "choose a channel to get data."

            // Show ports and paths of touched form
            for (PortImage portImage : getPortImages()) {
                List<Path> paths = portImage.getPort().getConnectedPaths();
                for (Path path : paths) {
                    // Show ports
                    getViz().getImage(path.getSource()).setVisibility(Visibility.VISIBLE);
                    getViz().getImage(path.getTarget()).setVisibility(Visibility.VISIBLE);
                    // Show path
                    getViz().getImage(path).setVisibility(Visibility.VISIBLE);
                }
            }

        } else if (touchInteraction.getType() == OnTouchActionListener.Type.HOLD) {
            // Log.v("onTouchInteraction", "TouchInteraction.HOLD to " + CLASS_NAME);
        } else if (touchInteraction.getType() == OnTouchActionListener.Type.MOVE) {
            // Log.v("onTouchInteraction", "TouchInteraction.MOVE to " + CLASS_NAME);
        } else if (touchInteraction.getType() == OnTouchActionListener.Type.TWITCH) {
            // Log.v("onTouchInteraction", "TouchInteraction.TWITCH to " + CLASS_NAME);
        } else if (touchInteraction.getType() == OnTouchActionListener.Type.DRAG) {
            // Log.v("onTouchInteraction", "TouchInteraction.DRAG to " + CLASS_NAME);
        } else if (touchInteraction.getType() == OnTouchActionListener.Type.RELEASE) {
            // Log.v("onTouchInteraction", "TouchInteraction.RELEASE to " + CLASS_NAME);
        }
    }
}
