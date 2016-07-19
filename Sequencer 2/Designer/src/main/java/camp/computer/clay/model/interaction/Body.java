package camp.computer.clay.model.interaction;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import camp.computer.clay.application.R;

import java.util.ArrayList;

import camp.computer.clay.application.Application;
import camp.computer.clay.model.simulation._Actor;
import camp.computer.clay.model.simulation.Path;
import camp.computer.clay.model.simulation.Port;
import camp.computer.clay.visualization.architecture.Image;
import camp.computer.clay.visualization.architecture.ImageGroup;
import camp.computer.clay.visualization.images.FormImage;
import camp.computer.clay.visualization.images.PathImage;
import camp.computer.clay.visualization.images.PortImage;
import camp.computer.clay.visualization.architecture.Visualization;
import camp.computer.clay.visualization.util.Geometry;
import camp.computer.clay.visualization.util.Point;
import camp.computer.clay.visualization.util.Rectangle;

public class Body extends _Actor {

    private Perspective perspective;

    private ArrayList<TouchInteractivity> touchInteractivities = new ArrayList<>();

    public Body() {
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    public Perspective getPerspective() {
        return this.perspective;
    }

    private void adjustPerspectivePosition() {
        getPerspective().setPosition(getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).calculateCentroid());
//        getPerspective().setPosition(getPerspective().getVisualization().getList().filterType(FormImage.TYPE).calculateCenter());
    }

    public void adjustPerspectiveScale() {
        ArrayList<Point> formImagePositions = getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).getPositions();
        if (formImagePositions.size() > 0) {
            Rectangle boundingBox = Geometry.calculateBoundingBox(formImagePositions);
            adjustPerspectiveScale(boundingBox);
        }
    }

    public void adjustPerspectiveScale(Rectangle boundingBox) {

        double horizontalDifference = boundingBox.getWidth() - getPerspective().getWidth();
        double verticalDifference = boundingBox.getHeight() - getPerspective().getHeight();

        double boundingBoxPadding = 0;
        double horizontalScale = getPerspective().getWidth() / boundingBox.getWidth();
        double verticalScale = getPerspective().getHeight() / boundingBox.getHeight();

//        Log.v("Perspective", "boundingWidth: " + boundingBox.getWidth());
//        Log.v("Perspective", "perspectiveWidth: " + getPerspective().getWidth());
//
//        Log.v("Perspective", "boundingHeight: " + boundingBox.getHeight());
//        Log.v("Perspective", "perspectiveHeight: " + getPerspective().getHeight());
//
//        Log.v("Perspective", "widthDifference: " + widthDifference);
//        Log.v("Perspective", "heightDifference: " + heightDifference);

        if (horizontalDifference > 0 && horizontalDifference > verticalDifference) {
            getPerspective().setScale(horizontalScale);
        } else if (verticalDifference > 0 && verticalDifference > horizontalDifference) {
            getPerspective().setScale(verticalScale);
        } else {
            getPerspective().setScale(1.0f);
        }
    }

    public TouchInteractivity getLatestTouchInteractivity() {
        if (touchInteractivities.size() > 0) {
            return this.touchInteractivities.get(touchInteractivities.size() - 1);
        } else {
            return null;
        }
    }

    public void onStartInteractivity(TouchInteraction touchInteraction) {
        Log.v("Body", "onStartInteractivity");

        // Having an idea is just accumulating intention. It's a suggestion from your existential
        // controller.

        TouchInteractivity touchInteractivity = new TouchInteractivity();




        Image nearestImage = getPerspective().getVisualization().getNearestImage(touchInteraction.getPosition());
        Log.v("NearestImage", "nearestImage: " + nearestImage);




        touchInteractivity.add(touchInteraction);

        touchInteractivities.add(touchInteractivity);

        // TODO: Cache and store the touch interactivites before deleting them completely! Do it in
        // TODO: (cont'd) a background thread.
        if (touchInteractivities.size() > 3) {
            touchInteractivities.remove(0);
        }

        onTouchListener(touchInteractivity, touchInteraction);
    }

    public void onContinueInteractivity(TouchInteraction touchInteraction) {
        Log.v("Body", "onContinueInteractivity");

        // Current
        touchInteraction.isTouching[touchInteraction.pointerIndex] = true;

        TouchInteractivity touchInteractivity = getLatestTouchInteractivity();

        // Calculate drag distance
        touchInteractivity.dragDistance[touchInteraction.pointerIndex] = Geometry.calculateDistance(touchInteraction.getPosition(), touchInteractivity.getFirst().touchPositions[touchInteraction.pointerIndex]);

        // Classify/Callback
        if (touchInteractivity.dragDistance[touchInteraction.pointerIndex] < TouchInteraction.MINIMUM_DRAG_DISTANCE) {
            // Pre-dragging
            onTwitchListener(touchInteractivity, touchInteraction);
        } else {
            // Dragging
            onDragListener(touchInteractivity, touchInteraction);
        }

        touchInteractivity.add(touchInteraction);
    }

    public void onCompleteInteractivity(TouchInteraction touchInteraction) {
        Log.v("Body", "onCompleteInteractivity");

        TouchInteractivity touchInteractivity = getLatestTouchInteractivity();

        // Stop listening for a hold interaction
        touchInteractivity.timerHandler.removeCallbacks(touchInteractivity.timerRunnable);

        // Current
        touchInteraction.isTouching[touchInteraction.pointerIndex] = false;

        // Classify/Callbacks
        if (touchInteractivity.isHolding[touchInteraction.pointerIndex]) {
            onReleaseListener(touchInteractivity, touchInteraction);
        } else {
            if (touchInteractivity.getDuration() < TouchInteraction.MAXIMUM_TAP_DURATION) {
                onTapListener(touchInteractivity, touchInteraction);
            } else {
                onPressListener(touchInteractivity, touchInteraction);
            }
        }

        touchInteractivity.add(touchInteraction);
    }

    private void onTouchListener(TouchInteractivity touchInteractivity, TouchInteraction touchInteraction) {

        touchInteraction.setType(TouchInteraction.Type.TOUCH);

        // Current
        touchInteraction.isTouching[touchInteraction.pointerIndex] = true;

        // First
        if (touchInteraction == touchInteractivity.getFirst()) {

            // TODO: Move touchPositions checking into Visualization.getTouchedSprite(Body);

            if (getPerspective().hasFocusImage()
                    && getPerspective().getFocusImage().isType(FormImage.TYPE, PortImage.TYPE, PathImage.TYPE)) {
                for (FormImage formImage : getPerspective().getVisualization().getFormImages()) {

                    if (!touchInteractivity.isTouchingImage()) {
                        for (PortImage portImage: formImage.getPortImages()) {

                            // If perspective is on path, then constraint interactions to ports in the path
                            if (getPerspective().getFocusImage().isType(PathImage.TYPE)) {
                                PathImage focusedPathImage = (PathImage) getPerspective().getFocusImage();
                                if (!focusedPathImage.getPath().contains((Port) portImage.getModel())) {
                                    // Log.v("InteractionHistory", "Skipping port not in path.");
                                    continue;
                                }
                            }

                            if (portImage.isTouching(touchInteraction.getPosition(), 50)) {

                                // Interactivity
                                touchInteractivity.setTouchedImage(touchInteraction.pointerIndex, portImage);

                                // Perspective
                                if (getPerspective().getFocusImage().isType(PathImage.TYPE)) {
                                    PathImage focusedPathImage = (PathImage) getPerspective().getFocusImage();
                                    Path path = (Path) focusedPathImage.getModel();
                                    if (path.getSource() == portImage.getPort()) {
                                        // <PERSPECTIVE>
                                        getPerspective().setFocus(portImage);
                                        getPerspective().setAdjustability(false);
                                        // </PERSPECTIVE>
                                    }
                                } else {
                                    // <PERSPECTIVE>
                                    getPerspective().setFocus(portImage);
                                    getPerspective().setAdjustability(false);
                                    // </PERSPECTIVE>
                                }

                                // TODO: Action

//                                    // <TOUCH_ACTION>
//                                    TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.TOUCH);
//                                    portSprite.touchPositions(touchInteraction);
//                                    // </TOUCH_ACTION>

                                break;
                            }
                        }
                    }
                }
            }

            if (getPerspective().hasFocusImage()
                    && getPerspective().getFocusImage().isType(PortImage.TYPE, PathImage.TYPE)
                    ) {
                for (FormImage formImage : getPerspective().getVisualization().getFormImages()) {
                    if (!touchInteractivity.isTouchingImage()) {
                        for (PortImage portImage : formImage.getPortImages()) {
                            for (PathImage pathImage: portImage.getPathImages()) {

                                PortImage sourcePortImage = (PortImage) getPerspective().getVisualization().getImage(pathImage.getPath().getSource());
                                PortImage targetPortImage = (PortImage) getPerspective().getVisualization().getImage(pathImage.getPath().getTarget());

                                double distanceToLine = (double) Geometry.calculateLineToPointDistance(
                                        // TODO: getPerspective().getVisualization().getImage(<Port/Model>)
                                        sourcePortImage.getPosition(),
                                        targetPortImage.getPosition(),
                                        touchInteraction.getPosition(),
                                        true
                                );

                                //Log.v("DistanceToLine", "distanceToLine: " + distanceToLine);

                                if (distanceToLine < 60) {

                                    Log.v("PathTouch", "start touchPositions on path " + pathImage);

//                                        // <TOUCH_ACTION>
//                                        TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.TOUCH);
//                                        pathSprite.touchPositions(touchInteraction);
//                                        // </TOUCH_ACTION>

                                    touchInteractivity.setTouchedImage(touchInteraction.pointerIndex, pathImage);

                                    // <PERSPECTIVE>
                                    getPerspective().setFocus(pathImage);
                                    getPerspective().setAdjustability(false);
                                    // </PERSPECTIVE>

                                    break;
                                }
                            }
                        }
                    }
                }

                // TODO: Check for touchPositions on path flow editor (i.e., spreadsheet or JS editors)
            }

            // Reset object interaction state
            if (!getPerspective().hasFocusImage()
                    || getPerspective().getFocusImage().isType(FormImage.TYPE, PortImage.TYPE)) {
                for (FormImage formImage : getPerspective().getVisualization().getFormImages()) {
                    // Log.v ("MapViewTouch", "Object at " + machineSprite.x + ", " + machineSprite.y);
                    // Check if one of the objects is touched
                    if (!touchInteractivity.isTouchingImage()) {
                        if (formImage.isTouching(touchInteraction.getPosition())) {

//                                // <TOUCH_ACTION>
//                                TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.TOUCH);
//                                machineSprite.touchPositions(touchInteraction);
//                                // </TOUCH_ACTION>

                            // TODO: Add this to an onTouch callback for the sprite's channel nodes
                            // TODO: i.e., callback Image.onTouch (via Image.touchPositions())

                            touchInteractivity.setTouchedImage(touchInteraction.pointerIndex, formImage);

                            // <PERSPECTIVE>
                            getPerspective().setFocus(formImage);
                            getPerspective().setAdjustability(false);
                            // </PERSPECTIVE>

                            // Break to limit the number of objects that can be touchPositions by a finger to one (1:1 finger:touchPositions relationship).
                            break;

                        }
                    }
                }
            }

            if (!getPerspective().hasFocusImage()
                    || getPerspective().getFocusImage().isType(FormImage.TYPE, PortImage.TYPE, PathImage.TYPE)) {

                // Touch the canvas
                if (!touchInteractivity.isTouchingImage()) {

                    // <INTERACTION>
//                    touchInteractivity.isTouchingImage[touchInteraction.pointerIndex] = false;
                    // </INTERACTION>

                    // <PERSPECTIVE>
                    this.getPerspective().setFocus(null);
                    // this.isAdjustable = false;
                    // </PERSPECTIVE>
                }
            }
        }

    }

    private void onTapListener(TouchInteractivity touchInteractivity, TouchInteraction touchInteraction) {

        if (touchInteractivity.isTouchingImage()
            && touchInteractivity.getTouchedImage().isType(FormImage.TYPE)) {

            // Touched Form

            FormImage formImage = (FormImage) touchInteractivity.getTouchedImage();

            // TODO: Add this to an onTouch callback for the sprite's channel nodes
            // Check if the touched board's I/O node is touched
            // Check if one of the objects is touched
            if (formImage.isTouching(touchInteraction.getPosition())) {
                Log.v("MapView", "\tTouched machine.");

                // ApplicationView.getDisplay().speakPhrase(machine.getNameTag());

                // <TOUCH_ACTION>
//                TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.TAP);
                // TODO: propagate RELEASE before TAP
                formImage.touch(touchInteraction);
                // </TOUCH_ACTION>

                // Remove focus from other form
                ImageGroup otherFormImages = getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).remove(formImage);
                for (Image image: otherFormImages.getList()) {
                    FormImage otherFormImage = (FormImage) image;
                    otherFormImage.hidePortImages();
                    otherFormImage.hidePathImages();
                    otherFormImage.setTransparency(0.1f);
                }

                // Focus on touched form
                formImage.showPortImages();
                formImage.showPathImages();
                formImage.setTransparency(1.0f);

                // TODO: Speak "choose a channel to get data."

                // Show ports and paths of touched form
                for (PortImage portImage: formImage.getPortImages()) {
                    ArrayList<Path> paths = getPerspective().getVisualization().getSimulation().getPathsByPort(portImage.getPort());
                    for (Path path: paths) {
                        // Show ports
                        getPerspective().getVisualization().getImage(path.getSource()).setVisibility(true);
                        getPerspective().getVisualization().getImage(path.getTarget()).setVisibility(true);
                        // Show path
                        getPerspective().getVisualization().getImage(path).setVisibility(true);
                    }
                }

                TouchInteractivity previousInteractivity = null;
                if (touchInteractivities.size() > 1) {
                    previousInteractivity = touchInteractivities.get(touchInteractivities.size() - 2);
                    Log.v("PreviousTouch", "Previous: " + previousInteractivity.getTouchedImage());
                    Log.v("PreviousTouch", "Current: " + touchInteractivity.getTouchedImage());
                }

                // Perspective
                if (formImage.getForm().getPaths().size() > 0
                        && (previousInteractivity != null && previousInteractivity.getTouchedImage() != touchInteractivity.getTouchedImage())) {

                    // Get ports along every path connected to the ports on the touched form
                    ArrayList<Port> formPathPorts = new ArrayList<>();
                    for (Port port: formImage.getForm().getPorts()) {

                        // TODO: ((PortImage) getPerspective().getVisualization().getImage(port)).getVisiblePaths()

                        if (!formPathPorts.contains(port)) {
                            formPathPorts.add(port);
                        }

                        ArrayList<Path> portPaths = getPerspective().getVisualization().getSimulation().getPathsByPort(port);
                        for (Path path: portPaths) {
                            if (!formPathPorts.contains(path.getSource())) {
                                formPathPorts.add(path.getSource());
                            }
                            if (!formPathPorts.contains(path.getTarget())) {
                                formPathPorts.add(path.getTarget());
                            }
                        }
                    }

                    // Perspective
                    ArrayList<Image> formPathPortImages = getPerspective().getVisualization().getImages(formPathPorts);

                    ArrayList<Point> formPortPositions = Visualization.getPositions(formPathPortImages);
                    Rectangle boundingBox = Geometry.calculateBoundingBox(formPortPositions);

                    getPerspective().setAdjustability(false);
                    adjustPerspectiveScale(boundingBox);
                    getPerspective().setPosition(boundingBox.getPosition());

                } else {

                    // Do this on second press, or when none of the machine's ports have paths.
                    // This provides lookahead, so you can be triggered to touch again to recover
                    // the perspective.

                    for (PortImage portImage: formImage.getPortImages()) {
                        ArrayList<PathImage> pathImages = portImage.getPathImages();
                        for (PathImage pathImage: pathImages) {
                            pathImage.setVisibility(false);
                        }
                    }

                    // TODO: (on second press, also hide external ports, expose peripherals) getPerspective().setScale(1.2f);
                    // TODO: (cont'd) getPerspective().setPosition(formImage.getPosition());

                    getPerspective().setAdjustability(false);
                    getPerspective().setScale(1.2f);
                    getPerspective().setPosition(formImage.getPosition());
                }
            }


        } else if (touchInteractivity.isTouchingImage()
                && touchInteractivity.getTouchedImage().isType(PortImage.TYPE)) {

            // Touched Port

            PortImage portImage = (PortImage) touchInteractivity.getTouchedImage();

            Log.v("Body", "\tPort " + (portImage.getIndex() + 1) + " touched.");

            if (portImage.isTouching(touchInteraction.getPosition())) {
//                TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.TAP);
                portImage.touch(touchInteraction);

                Log.v("Body", "\tSource port " + (portImage.getIndex() + 1) + " touched.");

                Port port = portImage.getPort();

                TouchInteractivity previousInteractivity = null;
                if (touchInteractivities.size() > 1) {
                    previousInteractivity = touchInteractivities.get(touchInteractivities.size() - 2);
                    Log.v("PreviousTouch", "Previous: " + previousInteractivity.getTouchedImage());
                    Log.v("PreviousTouch", "Current: " + touchInteractivity.getTouchedImage());
                }

                TouchInteractivity previousInteractivity2 = null;
                if (touchInteractivities.size() > 2) {
                    previousInteractivity2 = touchInteractivities.get(touchInteractivities.size() - 3);
                    Log.v("PreviousTouch", "Previous: " + previousInteractivity2.getTouchedImage());
                    Log.v("PreviousTouch", "Current: " + touchInteractivity.getTouchedImage());
                }

                if (previousInteractivity != null && previousInteractivity.getTouchedImage() == getPerspective().getVisualization().getImage(port.getForm())
                        && previousInteractivity2 != null && previousInteractivity2.getTouchedImage() == getPerspective().getVisualization().getImage(port.getForm())) {

                    Log.v("Interaction_Model", "AUX");
                }



                if (port.getType() == Port.Type.NONE) {

                    port.setDirection(Port.Direction.INPUT);
                    port.setType(Port.Type.getNextType(port.getType()));

                    // TODO: Speak ~ "setting as input. you can send the data to another board if you want. touchPositions another board."

                } else if (!port.hasPath() && getPerspective().getVisualization().getSimulation().getAncestorPathsByPort(port).size() == 0) {

                    // TODO: Replace with state of perspective. i.e., Check if seeing a single path.

                    Port.Type nextType = port.getType();
                    while ((nextType == Port.Type.NONE) || (nextType == port.getType())) {
                        nextType = Port.Type.getNextType(nextType);
                    }
                    port.setType(nextType);

                } else if (!portImage.hasVisiblePaths() && !portImage.hasVisibleAncestorPaths()) {

                    // TODO: Replace hasVisiblePaths() with check for focusedSprite/Path

                    // TODO: If second press, change the channel.

                    // Remove focus from other machines and their ports.
                    for (FormImage formImage : getPerspective().getVisualization().getFormImages()) {
                        formImage.setTransparency(0.05f);
                        formImage.hidePortImages();
                        formImage.hidePathImages();
                    }

                    // Reduce focus on the machine
                    portImage.getFormImage().setTransparency(0.05f);

                    // Focus on the port
                    //portImage.getFormImage().showPathImage(portImage.getIndex(), true);
                    portImage.showPaths();
                    portImage.setVisibility(true);
                    portImage.setPathVisibility(true);

                    ArrayList<Path> paths = getPerspective().getVisualization().getSimulation().getPathsByPort(portImage.getPort());
                    for (Path connectedPath: paths) {
                        // Show ports
                        getPerspective().getVisualization().getImage(connectedPath.getSource()).setVisibility(true);
                        ((PortImage) getPerspective().getVisualization().getImage(connectedPath.getSource())).showPaths();
                        getPerspective().getVisualization().getImage(connectedPath.getTarget()).setVisibility(true);
                        ((PortImage) getPerspective().getVisualization().getImage(connectedPath.getTarget())).showPaths();
                        // Show path
                        getPerspective().getVisualization().getImage(connectedPath).setVisibility(true);
                    }

                    // ApplicationView.getDisplay().speakPhrase("setting as input. you can send the data to another board if you want. touchPositions another board.");

                    // Perspective
                    ArrayList<Port> pathPorts = getPerspective().getVisualization().getSimulation().getPortsInPaths(paths);
                    ArrayList<Image> pathPortImages = getPerspective().getVisualization().getImages(pathPorts);
                    ArrayList<Point> pathPortPositions = Visualization.getPositions(pathPortImages);
                    Rectangle boundingBox = Geometry.calculateBoundingBox(pathPortPositions);
                    adjustPerspectiveScale(boundingBox);

                    getPerspective().setPosition(Geometry.calculateCenterPosition(pathPortPositions));

                } else if (portImage.hasVisiblePaths() || portImage.hasVisibleAncestorPaths()) {

                    // Paths are being shown. Touching a port changes the port type. This will also
                    // updates the corresponding path requirement.

                    // TODO: Replace with state of perspective. i.e., Check if seeing a single path.

                    Port.Type nextType = port.getType();
                    while ((nextType == Port.Type.NONE) || (nextType == port.getType())) {
                        nextType = Port.Type.getNextType(nextType);
                    }
                    port.setType(nextType);

                }
            }

        } else if (touchInteractivity.isTouchingImage()
                && touchInteractivity.getTouchedImage().isType(PathImage.TYPE)) {

            PathImage pathImage = (PathImage) touchInteractivity.getTouchedImage();

//            if (pathImage.getEditorVisibility()) {
//                pathImage.setEditorVisibility(false);
//            } else {
//                pathImage.setEditorVisibility(true);
//            }

            // TODO: Show path programmer (Create and curate actions built in JS by inserting
            // TODO: (cont'd) exposed ports into action ports. This defines a path)
            final RelativeLayout timelineView = (RelativeLayout) Application.getDisplay().findViewById(R.id.path_editor_view);
            timelineView.setVisibility(View.VISIBLE);


        } else if (!touchInteractivity.isTouchingImage()) {

            // No touchPositions on board or port. Touch is on map. So hide ports.
            for (FormImage formImage : getPerspective().getVisualization().getFormImages()) {
                formImage.hidePortImages();
                formImage.hidePathImages();
                formImage.setTransparency(1.0f);
            }

            ArrayList<Point> formImagePositions = getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).getPositions();
            Point formImagesCenterPosition = Geometry.calculateCenterPosition(formImagePositions);

            adjustPerspectiveScale();

            //getPerspective().setPosition(getPerspective().getVisualization().getList().filterType(FormImage.TYPE).calculateCentroid());
            getPerspective().setPosition(formImagesCenterPosition);

            // Reset map interactivity
            getPerspective().setAdjustability(true);
        }

    }

    private void onPressListener(TouchInteractivity touchInteractivity, TouchInteraction touchInteraction) {

        if (touchInteractivity.isTouchingImage()
                && touchInteractivity.getTouchedImage().isType(FormImage.TYPE)) {
            FormImage formImage = (FormImage) touchInteractivity.getTouchedImage();

            // TODO: Add this to an onTouch callback for the sprite's channel nodes
            // Check if the touched board's I/O node is touched
            // Check if one of the objects is touched
            if (Geometry.calculateDistance(touchInteractivity.getFirst().touchPositions[touchInteraction.pointerIndex], formImage.getPosition()) < 80) {
                Log.v("MapView", "\tSource board touched.");

//                    // <TOUCH_ACTION>
//                    TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.TAP);
//                    // TODO: propagate RELEASE before TAP
//                    machineSprite.touchPositions(touchInteraction);
//                    // </TOUCH_ACTION>

                // No touchPositions on board or port. Touch is on map. So hide ports.
                for (FormImage otherFormImage : getPerspective().getVisualization().getFormImages()) {
                    otherFormImage.hidePortImages();
                    otherFormImage.hidePathImages();
                    otherFormImage.setTransparency(0.1f);
                }
                formImage.showPortImages();
//                getPerspective().setScale(0.8f);
                formImage.showPathImages();
                formImage.setTransparency(1.0f);
                // ApplicationView.getDisplay().speakPhrase("choose a channel to get data.");

                getPerspective().setAdjustability(false);
            }

            // Zoom out to show overview
//            getPerspective().setScale(1.0f);

        } else if (touchInteractivity.isTouchingImage()
                && touchInteractivity.getTouchedImage().isType(PortImage.TYPE)) {

            PortImage portImage = (PortImage) touchInteractivity.getTouchedImage();
//            TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.RELEASE);
            portImage.touch(touchInteraction);

            // Show ports of nearby machines
            boolean useNearbyPortImage = false;
            for (FormImage nearbyFormImage : getPerspective().getVisualization().getFormImages()) {

                // Update style of nearby machines
                double distanceToMachineImage = (double) Geometry.calculateDistance(
                        touchInteraction.getPosition(),
                        nearbyFormImage.getPosition()
                );

                if (distanceToMachineImage < nearbyFormImage.boardHeight + 50) {

                    // TODO: use overlappedImage instanceof PortImage

                    for (PortImage nearbyPortImage: nearbyFormImage.getPortImages()) {

                        if (nearbyPortImage != portImage) {
                            if (nearbyPortImage.isTouching(touchInteraction.getPosition(), 50)) {

                                Port port = (Port) portImage.getModel();
                                Port nearbyPort = (Port) nearbyPortImage.getModel();

                                useNearbyPortImage = true;

                                if (port.getDirection() == Port.Direction.NONE) {
                                    port.setDirection(Port.Direction.INPUT);
                                }
                                if (port.getType() == Port.Type.NONE) {
                                    port.setType(Port.Type.getNextType(port.getType())); // (machineSprite.channelTypes.get(i) + 1) % machineSprite.channelTypeColors.length
                                }

                                nearbyPort.setDirection(Port.Direction.OUTPUT);
                                nearbyPort.setType(Port.Type.getNextType(nearbyPort.getType()));

                                // Create and add path to port
                                Port sourcePort = (Port) getPerspective().getVisualization().getModel(portImage);
                                Port targetPort = (Port) getPerspective().getVisualization().getModel(nearbyPortImage);

                                if (!getPerspective().getVisualization().getSimulation().hasAncestor(sourcePort, targetPort)) {

                                    if (sourcePort.getPaths().size() == 0) {

                                        Path path = new Path(sourcePort, targetPort);
                                        sourcePort.addPath(path);

                                        PathImage pathImage = new PathImage(path);
                                        pathImage.setVisualization(getPerspective().getVisualization());
                                        getPerspective().getVisualization().addImage(path, pathImage, "paths");

                                        PortImage targetPortImage = (PortImage) getPerspective().getVisualization().getImage(path.getTarget());
                                        if (targetPort.getPaths().size() == 0) {
                                            targetPortImage.setUniqueColor(portImage.getUniqueColor());
                                        }

                                        // Remove focus from other machines and their ports.
                                        for (FormImage formImage : getPerspective().getVisualization().getFormImages()) {
                                            formImage.setTransparency(0.05f);
                                            formImage.hidePortImages();
                                            formImage.hidePathImages();
                                        }

                                        portImage.setVisibility(true);
                                        portImage.showPaths();
                                        targetPortImage.setVisibility(true);
                                        targetPortImage.showPaths();
                                        pathImage.setVisibility(true);

                                        ArrayList<Path> paths = getPerspective().getVisualization().getSimulation().getPathsByPort(targetPort);
                                        for (Path connectedPath : paths) {
                                            // Show ports
                                            ((PortImage) getPerspective().getVisualization().getImage(connectedPath.getSource())).setVisibility(true);
                                            ((PortImage) getPerspective().getVisualization().getImage(connectedPath.getSource())).showPaths();
                                            ((PortImage) getPerspective().getVisualization().getImage(connectedPath.getTarget())).setVisibility(true);
                                            ((PortImage) getPerspective().getVisualization().getImage(connectedPath.getTarget())).showPaths();
                                            // Show path
                                            getPerspective().getVisualization().getImage(connectedPath).setVisibility(true);
                                        }

                                        // ApplicationView.getDisplay().speakPhrase("setting as input. you can send the data to another board if you want. touchPositions another board.");

                                        // Perspective
                                        ArrayList<Port> pathPorts = getPerspective().getVisualization().getSimulation().getPortsInPaths(paths);
                                        ArrayList<Image> pathPortImages = getPerspective().getVisualization().getImages(pathPorts);
                                        ArrayList<Point> pathPortPositions = Visualization.getPositions(pathPortImages);
                                        getPerspective().setPosition(Geometry.calculateCenterPosition(pathPortPositions));

                                        Rectangle boundingBox = Geometry.calculateBoundingBox(pathPortPositions);

//                                        Log.v("Images", "pathPortImages.size = " + pathPortImages.size());
//                                        Log.v("Images", "pathPortPositions.size = " + pathPortPositions.size());
//                                        for (Point pathPortPosition: pathPortPositions) {
//                                            Log.v("Images", "x: " + pathPortPosition.x + ", y: " + pathPortPosition.y);
//                                        }
//                                        Log.v("Images", "boundingBox.length = " + boundingBox.length);
//                                        for (double boundingPosition: boundingBox) {
//                                            Log.v("Images", "bounds: " + boundingPosition);
//                                        }

                                        adjustPerspectiveScale(boundingBox);

                                    } else {

                                        Path path = new Path(sourcePort, targetPort);
                                        sourcePort.addPath(path);

                                        PathImage pathImage = new PathImage(path);
                                        pathImage.setVisualization(getPerspective().getVisualization());
                                        getPerspective().getVisualization().addImage(path, pathImage, "paths");

                                        PortImage targetPortImage = (PortImage) getPerspective().getVisualization().getImage(path.getTarget());
                                        targetPortImage.setUniqueColor(portImage.getUniqueColor());
//                                        portImage.pathImages.add(pathImage);

                                        // Remove focus from other machines and their ports.
                                        for (FormImage formImage : getPerspective().getVisualization().getFormImages()) {
                                            formImage.setTransparency(0.05f);
                                            formImage.hidePortImages();
                                            formImage.hidePathImages();
                                        }

                                        portImage.setVisibility(true);
                                        portImage.showPaths();
                                        targetPortImage.setVisibility(true);
                                        targetPortImage.showPaths();
                                        pathImage.setVisibility(true);

                                        ArrayList<Path> paths = getPerspective().getVisualization().getSimulation().getPathsByPort(targetPort);
                                        for (Path connectedPath : paths) {
                                            // Show ports
                                            (getPerspective().getVisualization().getImage(connectedPath.getSource())).setVisibility(true);
                                            ((PortImage) getPerspective().getVisualization().getImage(connectedPath.getSource())).showPaths();
                                            (getPerspective().getVisualization().getImage(connectedPath.getTarget())).setVisibility(true);
                                            ((PortImage) getPerspective().getVisualization().getImage(connectedPath.getTarget())).showPaths();
                                            // Show path
                                            getPerspective().getVisualization().getImage(connectedPath).setVisibility(true);
                                        }

                                        // ApplicationView.getDisplay().speakPhrase("setting as input. you can send the data to another board if you want. touchPositions another board.");

                                        // Perspective
                                        ArrayList<Port> pathPorts = getPerspective().getVisualization().getSimulation().getPortsInPaths(paths);
                                        ArrayList<Image> pathPortImages = getPerspective().getVisualization().getImages(pathPorts);
                                        ArrayList<Point> pathPortPositions = Visualization.getPositions(pathPortImages);
                                        getPerspective().setPosition(Geometry.calculateCenterPosition(pathPortPositions));

                                        Rectangle boundingBox = Geometry.calculateBoundingBox(pathPortPositions);

//                                        Log.v("Images", "pathPortImages.size = " + pathPortImages.size());
//                                        Log.v("Images", "pathPortPositions.size = " + pathPortPositions.size());
//                                        for (Point pathPortPosition: pathPortPositions) {
//                                            Log.v("Images", "x: " + pathPortPosition.x + ", y: " + pathPortPosition.y);
//                                        }
//                                        Log.v("Images", "boundingBox.length = " + boundingBox.length);
//                                        for (double boundingPosition: boundingBox) {
//                                            Log.v("Images", "bounds: " + boundingPosition);
//                                        }

                                        adjustPerspectiveScale(boundingBox);

                                    }
                                }

                                // TODO: Vibrate

                                touchInteraction.setOverlappedImage(null);

                                break;
                            }
                        }
                    }
                }
            }

            Log.v("TouchPort", "Touched port");

            if (!useNearbyPortImage) {

                Port port = (Port) portImage.getModel();

                port.setDirection(Port.Direction.INPUT);

                if (port.getType() == Port.Type.NONE) {
                    port.setType(Port.Type.getNextType(port.getType()));
                }
            }

        } else if (touchInteractivity.isTouchingImage()
                && touchInteractivity.getTouchedImage().isType(PathImage.TYPE)) {

            PathImage pathImage = (PathImage) touchInteractivity.getTouchedImage();

        } else if (!touchInteractivity.isTouchingImage()) {

//            // No touchPositions on board or port. Touch is on map. So hide ports.
//            for (FormImage formImage : getPerspective().getVisualization().getFormImages()) {
//                formImage.hidePortImages();
//                formImage.hidePathImages();
//                formImage.setTransparency(1.0f);
//            }
//
//            // Adjust panning
//            // Auto-adjust the perspective
//            Point centroidPosition = getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).calculateCentroid();
//            getPerspective().setPosition(new Point(centroidPosition.x, centroidPosition.y));
//
//            adjustPerspectiveScale();

        }

        // Reset map interactivity
        getPerspective().setAdjustability(true);
    }

    public void onHoldListener(TouchInteractivity touchInteractivity, TouchInteraction touchInteraction) {
        Log.v("MapViewEvent", "onHoldListener");

//        if (touchInteractivity.dragDistance[touchInteraction.pointerIndex] < TouchInteraction.MINIMUM_DRAG_DISTANCE) {
            // Holding but not (yet) dragging.

        touchInteractivity.isHolding[touchInteraction.pointerIndex] = true;

        // Show ports for sourceMachine board
        if (touchInteractivity.isTouchingImage()) {
            if (touchInteractivity.getTouchedImage().isType(FormImage.TYPE)) {

//                FormImage machineImage = (FormImage) touchInteractivity.touchedImage[touchInteraction.pointerIndex];
////                    TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.HOLD);
//                machineImage.touch(touchInteraction);
//
//                //machineSprite.showPortImages();
//                //machineSprite.showPathImages();
//                //touchSourceSprite = machineSprite;
//                getPerspective().setScale(0.8f);

            } else if (touchInteractivity.getTouchedImage().isType(PortImage.TYPE)) {

                Log.v("Holding", "Holding port");

//                PortImage portImage = (PortImage) touchInteractivity.touchedImage[touchInteraction.pointerIndex];
////                    TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.HOLD);
//                portImage.touch(touchInteraction);
//
////                    portSprite.showPortImages();
////                    portSprite.showPathImages();
//                getPerspective().setScale(0.8f);

            }
        }
//        }

        // TODO: add onHoldForDuration(duration) callback, to set multiple hold callbacks (general interface).
        // TODO: ^ do same for consecutive taps in the same area.
    }

    private void onTwitchListener(TouchInteractivity touchInteractivity, TouchInteraction touchInteraction) {

        // TODO: Encapsulate TouchInteraction in TouchEvent
//        TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.TWITCH);
//        touchInteractivity.add(touchInteraction);

    }

    private void onDragListener(TouchInteractivity touchInteractivity, TouchInteraction touchInteraction) {
        Log.v("Body", "onDragListener");

        touchInteractivity.isDragging[touchInteraction.pointerIndex] = true;

        // Dragging and holding
        if (touchInteractivity.isHolding[touchInteraction.pointerIndex]) {

            // Holding and dragging

            // TODO: Put into callback
            if (touchInteractivity.isTouchingImage()) {

                if (touchInteractivity.getTouchedImage().isType(FormImage.TYPE)) {

                    FormImage formImage = (FormImage) touchInteractivity.getTouchedImage();
//                    TouchInteraction touchInteraction = new TouchInteraction(TouchInteraction.Type.DRAG);
                    formImage.touch(touchInteraction);
                    formImage.setPosition(new Point(touchInteraction.getPosition().getX(), touchInteraction.getPosition().getY()));

                    // Zoom out to show overview
//                    getPerspective().setScale(0.8f);

                } else if (touchInteractivity.getTouchedImage().isType(PortImage.TYPE)) {

                    PortImage portImage = (PortImage) touchInteractivity.getTouchedImage();
                    portImage.isTouched = true;
//                    TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.DRAG);
//                    portSprite.touchPositions(touchInteraction);

                    portImage.setPosition(touchInteraction.getPosition());
                }

            } else if (getPerspective().isAdjustable()) {

                getPerspective().setScale(0.9f);
                getPerspective().setOffset(
                        (int) (touchInteraction.getPosition().getX() - touchInteractivity.getFirst().getPosition().getX()),
                        (int) (touchInteraction.getPosition().getY() - touchInteractivity.getFirst().getPosition().getY())
                );

            }

        } else {

            // Dragging only (not holding)

            // TODO: Put into callback
            if (touchInteractivity.isTouchingImage()) {

                if (touchInteractivity.getTouchedImage().isType(FormImage.TYPE)) {

                    FormImage formImage = (FormImage) touchInteractivity.getTouchedImage();
                    formImage.touch(touchInteraction);
                    formImage.setPosition(touchInteraction.getPosition());

                    // Perspective (zoom out to show overview)
                    // adjustPerspectivePosition();
                    adjustPerspectiveScale();

                } else if (touchInteractivity.getTouchedImage().isType(PortImage.TYPE)) {

                    PortImage portImage = (PortImage) touchInteractivity.getTouchedImage();
//                    TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.DRAG);
//                    portSprite.touchPositions(touchInteraction);

                    portImage.setCandidatePathDestinationPosition(touchInteraction.getPosition());
                    portImage.setCandidatePathVisibility(true);

                    // Setup port type and flow direction
                    Port port = portImage.getPort();
                    if (port.getDirection() == Port.Direction.NONE) {
                        port.setDirection(Port.Direction.INPUT);
                    }
                    if (port.getType() == Port.Type.NONE) {
                        port.setType(Port.Type.getNextType(port.getType())); // (machineSprite.channelTypes.get(i) + 1) % machineSprite.channelTypeColors.length
                    }

                    // Show ports of nearby forms
                    ImageGroup nearbyImages = getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).filterDistance(touchInteraction.getPosition(), 200 + 60);
                    for (Image image: getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).getList()) {

                        if (image == portImage.getFormImage() || nearbyImages.contains(image)) {

                            FormImage nearbyFormImage = (FormImage) image;
                            nearbyFormImage.setTransparency(1.0f);
                            nearbyFormImage.showPortImages();

                        } else {

                            FormImage nearbyFormImage = (FormImage) image;
                            nearbyFormImage.setTransparency(0.1f);

                            // TODO: Fix the glitching caused by enabling this.
                            // nearbyFormImage.hidePortImages();

                        }
                    }

                    // Check if a machine sprite was nearby
                    Image nearestFormImage = getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).getNearestImage(touchInteraction.getPosition());
                    if (nearestFormImage != null) {

                        touchInteraction.setOverlappedImage(nearestFormImage);

                        // TODO: Vibrate

                        // Adjust perspective
                        //getPerspective().setPosition(nearestFormImage.getPosition());
                        getPerspective().setScale(0.3f, 100); // Zoom out to show overview

                    } else {

                        // Show ports and paths
                        portImage.setVisibility(true);
                        portImage.showPaths();

                        // Adjust perspective
                        getPerspective().setPosition(getPerspective().getVisualization().getImages().filterType(FormImage.TYPE).calculateCenter());
                        getPerspective().setScale(0.6f); // Zoom out to show overview

                    }

                    /*
                    // Show the ports in the path
                    ArrayList<Path> portPaths = getPerspective().getVisualization().getSimulation().getPathsByPort(port);
                    ArrayList<Port> portConnections = getPerspective().getVisualization().getSimulation().getPortsInPaths(portPaths);
                    for (Port portConnection: portConnections) {
                        PortImage portImageConnection = (PortImage) getPerspective().getVisualization().getImage(portConnection);
                        portImageConnection.setVisibility(true);
                        portImageConnection.showPathImages();
                    }
                    */
                }

            } else {

                // Dragging perspective

                Log.v("Drag", "moving perspective");

//                if (getPerspective().isAdjustable()) {
                    getPerspective().setScale(0.9f);
//                    getPerspective().setOffset(
//                            (int) (touchInteraction.getPosition().getX() - touchInteractivity.getFirst().getPosition().getX()),
//                            (int) (touchInteraction.getPosition().getY() - touchInteractivity.getFirst().getPosition().getY()));
                    getPerspective().setPosition(touchInteraction.getPosition(), 0);
//                        (int) (touchInteraction.getPosition().getX() - touchInteractivity.getFirst().getPosition().getX()),
//                        (int) (touchInteraction.getPosition().getY() - touchInteractivity.getFirst().getPosition().getY()));
//                }
            }

        }
    }

    private void onReleaseListener(TouchInteractivity touchInteractivity, TouchInteraction touchInteraction) {

        // Stop touching sprite
        // Style. Reset the style of touched boards.
        if (touchInteraction.isTouching[touchInteraction.pointerIndex] || touchInteractivity.isTouchingImage()) {

            touchInteraction.isTouching[touchInteraction.pointerIndex] = false;

            if (touchInteractivity.getTouchedImage().isType(FormImage.TYPE)) {

                // Touching Form

                FormImage formImage = (FormImage) touchInteractivity.getTouchedImage();

//                formImage.setScale(1.0f);
//                touchInteractivity.touchedImage[touchInteraction.pointerIndex] = null;

            } else if (touchInteractivity.getTouchedImage().isType(PortImage.TYPE)) {

                // Touching Port

                if (touchInteractivity.isHolding[touchInteraction.pointerIndex]) {

                    /*
                    PortImage portImage = (PortImage) touchInteractivity.touchedImage[touchInteraction.pointerIndex];
//            TouchInteraction touchInteraction = new TouchInteraction(touchInteraction.getPosition(), TouchInteraction.Type.RELEASE);
//                portImage.touch(touchInteraction);

                    // Show ports of nearby machines
                    for (FormImage nearbyMachineImage : getPerspective().getVisualization().getFormImages()) {

                        // Update style of nearby machines
                        double distanceToMachineImage = (double) Geometry.calculateDistance(
                                touchInteraction.getPosition(),
                                nearbyMachineImage.getPosition()
                        );

                        if (distanceToMachineImage < nearbyMachineImage.boardHeight + 50) {

                            // TODO: use overlappedImage instanceof PortImage

                            for (PortImage nearbyPortImage : nearbyMachineImage.getPortImages()) {

                                // Scaffold interaction to connect path to with nearby ports
                                double distanceToNearbyPortImage = (double) Geometry.calculateDistance(
                                        touchInteraction.getPosition(),
                                        nearbyPortImage.getPosition()
                                );

                                if (nearbyPortImage != portImage) {
                                    if (distanceToNearbyPortImage < nearbyPortImage.shapeRadius + 50) {

                                        // Create and add path to port
                                        Port sourcePort = (Port) portImage.getModel();
                                        Port destinationPort = (Port) nearbyPortImage.getModel();

                                        // Copy new port state
                                        destinationPort.setType(sourcePort.getType());
                                        destinationPort.setDirection(sourcePort.getDirection());

                                        // Update paths' source
                                        for (Path path: sourcePort.getPaths()) {
                                            path.setSource(destinationPort);
                                        }

                                        // Update ancestor paths' destination
                                        for (Path path: getPerspective().getVisualization().getSimulation().getPaths()) {
                                            if (path.getTarget() == sourcePort) {
                                                path.setTarget(destinationPort);
                                            }
                                        }

                                        // Reset old port state
                                        sourcePort.setType(Port.Type.NONE);
                                        sourcePort.setDirection(Port.Direction.NONE);

                                        break;
                                    }
                                }
                            }
                        }
                    }
                    */
                }
            }
        }

        if (touchInteractivity.isTouchingImage()) {
            touchInteractivity.getTouchedImage().isTouched = false;
        }

        touchInteractivity.isDragging[touchInteraction.pointerIndex] = false;
        touchInteractivity.isHolding[touchInteraction.pointerIndex] = false;

    }
}
