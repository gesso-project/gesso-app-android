package camp.computer.clay.engine.components;

import camp.computer.clay.util.geometry.Geometry;

/**
 * Colloquially, {@code TransformComponent} may be referred to as <em>PositionComponent</em>. In
 * equivalent ECS-style architectures, this is often the case.
 */
public class TransformComponent extends Component {

    /**
     * The x coordinate's position relative to {@code referencePoint}. If {@code referencePoint} is
     * {@code null} then this is equivalent to an absolute position.
     */
    public double x = 0;

    /**
     * The y coordinate's position relative to {@code referencePoint}. If {@code referencePoint} is
     * {@code null} then this is equivalent to an absolute position.
     */
    public double y = 0;

    /**
     * Relative rotation of the coordinate with which endpoints referencing this one will be
     * rotated.
     */
    public double rotation = 0;

    /**
     * Rotation rotation in degrees
     */
    public TransformComponent() {
        this(0, 0);
    }

    /**
     * Copy constructor. Creates a new {@code Point} object with properties identical to those of
     * {@code otherPoint}.
     *
     * @param otherPoint The {@code Point} to set.
     */
    public TransformComponent(TransformComponent otherPoint) {
        this.x = otherPoint.x;
        this.y = otherPoint.y;
    }

    public TransformComponent(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param point Absolute position. Converted to relative position internally.
     */
    public void set(TransformComponent point) {
        x = point.x;
        y = point.y;
        rotation = point.rotation;
    }

    public void set(TransformComponent point, TransformComponent referencePoint) {
        double x2 = Geometry.distance(0, 0, point.x, point.y) * Math.cos(Math.toRadians(referencePoint.rotation + Geometry.getAngle(0, 0, point.x, point.y)));
        this.x = referencePoint.x + x2;

        double y2 = Geometry.distance(0, 0, point.x, point.y) * Math.sin(Math.toRadians(referencePoint.rotation + Geometry.getAngle(0, 0, point.x, point.y)));
        this.y = referencePoint.y + y2;
    }

    public void set(double x, double y, TransformComponent referencePoint) {
        double x2 = Geometry.distance(0, 0, x, y) * Math.cos(Math.toRadians(referencePoint.rotation + Geometry.getAngle(0, 0, x, y)));
        this.x = referencePoint.x + x2;

        double y2 = Geometry.distance(0, 0, x, y) * Math.sin(Math.toRadians(referencePoint.rotation + Geometry.getAngle(0, 0, x, y)));
        this.y = referencePoint.y + y2;
    }

    /**
     * @param dx Offset along x axis from current x position.
     * @param dy Offset along y axis from current y position.
     */
    public void offset(double dx, double dy) {
        this.x = this.x + dx;
        this.y = this.y + dy;
    }

    public double getRotation() {
        return this.rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
}