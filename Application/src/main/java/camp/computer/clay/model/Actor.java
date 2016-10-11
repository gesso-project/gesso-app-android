package camp.computer.clay.model;

import java.util.LinkedList;
import java.util.List;

import camp.computer.clay.model.action.Action;
import camp.computer.clay.model.action.Camera;
import camp.computer.clay.model.action.Event;
import camp.computer.clay.util.image.Image;
import camp.computer.clay.util.image.Shape;
import camp.computer.clay.util.image.Space;

/**
 * {@code Actor} models a user of Clay and performs actions in the simulated world on user's behalf,
 * based on the actions recognized on one of the {@code PhoneHost} objects associated with the
 * {@code Actor}.
 */
public class Actor {

    private Camera camera = new Camera();

    private List<Action> actions = new LinkedList<>();

    public Actor() {
        setup();
    }

    private void setup() {
        Camera camera = new Camera();
        setCamera(camera);
    }

    /**
     * Sets the {@code Camera} that defines the {@code Actor}'s viewing area onto the {@code Space}.
     *
     * @param camera The {@code Camera} to use to define the viewing area onto the {@code Space}.
     */
    // TODO: Remove?
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    // TODO: Remove?
    public boolean hasCamera() {
        return camera != null;
    }

    // TODO: Remove?
    public Camera getCamera() {
        return this.camera;
    }

    public void queueAction(Event event) {

        event.setActor(this);

        switch (event.getType()) {

            case SELECT: {

                // Create a new Action
                Action action = new Action();
                action.setActor(this);
                actions.add(action);

                // Add Event to Action
                action.addEvent(event);

                // Record actions on timeline
                // TODO: Cache and store the queueAction actions before deleting them completely! Do it in
                // TODO: (cont'd) a background thread.
                if (actions.size() > 3) {
                    actions.remove(0);
                }

                processAction(action, event);

                break;
            }

            case HOLD: {

                // Start a new action
                Action action = getAction();
                actions.add(action);

                // Add event to action
                action.addEvent(event);

                processAction(action, event);

                break;
            }

            case MOVE: {

                Action action = getAction();
                action.addEvent(event);

                // Current
                event.isPointing[event.pointerIndex] = true;

                processAction(action, event);

                break;
            }

            case UNSELECT: {

                Action action = getAction();
                action.addEvent(event);

                // Current
                event.isPointing[event.pointerIndex] = false;

                processAction(action, event);

                break;
            }
        }
    }

    public void processAction(Action action, Event event) {

        switch (event.getType()) {

            case SELECT: {

                // Set the target image
                Image targetImage = getCamera().getSpace().getImage(event.getPosition());
                event.setTargetImage(targetImage);

                // Set the target shape
                Shape targetShape = targetImage.getShape(event.getPosition());
                event.setTargetShape(targetShape);

                // Action the event
                event.getTargetImage().processAction(action);
            }

            case HOLD: {

                // Set the target image
                Image targetImage = getCamera().getSpace().getImage(event.getPosition());
                event.setTargetImage(targetImage);

                // Set the target shape
                Shape targetShape = targetImage.getShape(event.getPosition());
                event.setTargetShape(targetShape);

                // Action the event
                event.getTargetImage().processAction(action);
            }

            case MOVE: {

                // Classify/Callback
                if (action.getDragDistance() > Event.MINIMUM_DRAG_DISTANCE) {

                    // Set the target image
                    Image targetImage = getCamera().getSpace().getImage(event.getPosition());
                    event.setTargetImage(targetImage);

                    // Set the target shape
                    Shape targetShape = targetImage.getShape(event.getPosition());
                    event.setTargetShape(targetShape);

                    action.getFirstEvent().getTargetImage().processAction(action);
                }
            }

            case UNSELECT: {

                // Stop listening for a hold event
                action.timerHandler.removeCallbacks(action.timerRunnable);

                // Set the target image
                Image targetImage = getCamera().getSpace().getImage(event.getPosition());
                event.setTargetImage(targetImage);

                // Set the target shape
                Shape targetShape = targetImage.getShape(event.getPosition());
                event.setTargetShape(targetShape);

                //event.getTargetImage().queueAction(action);
                action.getFirstEvent().getTargetImage().processAction(action);
            }
        }
    }


    /**
     * Returns the most recent interaction.
     *
     * @return The most recent interaction.
     */
    private Action getAction() {
        if (actions.size() > 0) {
            return actions.get(actions.size() - 1);
        } else {
            return null;
        }
    }

    public List<Action> getActions() {
        return this.actions;
    }

    public void update() {

    }

}
