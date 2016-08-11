package camp.computer.clay.model.architecture;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Model} is a simulation of the environmental context.
 */
public class Model extends Construct {

    private System system = new System();

    private List<Body> bodies = new ArrayList<>();

    private List<Frame> frames = new ArrayList<>();

    private List<Patch> patches = new ArrayList<>();

    public void addBody(Body body) {
        this.bodies.add(body);
    }

    public Body getBody(int index) {
        return this.bodies.get(index);
    }

    public List<Body> getBodies() {
        return this.bodies;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public System getSystem() {
        return this.system;
    }

    public void addFrame(Frame frame) {
        this.frames.add(frame);
        frame.setParent(this);
    }

    public Frame getFrame(int index) {
        return this.frames.get(index);
    }

    public List<Frame> getFrames() {
        return this.frames;
    }

    public List<Port> getPorts() {
        List<Port> ports = new ArrayList<>();
        for (Frame frame : this.frames) {
            ports.addAll(frame.getPorts());
        }
        return ports;
    }

    public List<Path> getPaths() {
        List<Path> paths = new ArrayList<>();
        for (Frame frame : this.frames) {
            for (Port port : frame.getPorts()) {
                paths.addAll(port.getPaths());
            }
        }
        return paths;
    }

    public void addPatch(Patch patch) {
        this.patches.add(patch);
        patch.setParent(this);
    }

    public Patch getPatch(int index) {
        return this.patches.get(index);
    }

    public List<Patch> getPatches() {
        return this.patches;
    }
}
