package camp.computer.clay.visualization.architecture;

import java.util.LinkedList;
import java.util.List;

import camp.computer.clay.application.Surface;
import camp.computer.clay.model.architecture.Construct;
import camp.computer.clay.model.interactivity.Action;
import camp.computer.clay.model.interactivity.ActionListener;
import camp.computer.clay.visualization.util.Color;
import camp.computer.clay.visualization.util.geometry.Geometry;
import camp.computer.clay.visualization.util.geometry.Point;
import camp.computer.clay.visualization.util.geometry.Rectangle;
import camp.computer.clay.visualization.util.geometry.Shape;
import camp.computer.clay.visualization.util.Visibility;

public abstract class Figure {

    protected List<Shape> shapes = new LinkedList<>();

    protected Point position = new Point(0, 0); // Figure position

    protected double scale = 1.0; // Figure scale factor

    protected Visibility visibility = Visibility.VISIBLE;

    protected double targetTransparency = 1.0;

    protected double transparency = targetTransparency;

    protected Construct construct = null;

    protected Visualization visualization = null;

    // TODO: Make this an interface? Move interface out of class.
    protected ActionListener actionListener;

    public Figure(Construct construct) {
        this.construct = construct;
    }

    public Construct getConstruct() {
        return this.construct;
    }

    public void setVisualization(Visualization visualization) {
        this.visualization = visualization;
    }

    public Visualization getVisualization() {
        return this.visualization;
    }

    public Figure getParentFigure() {
        if (getConstruct().hasParent()) {
            Construct parentConstruct = getConstruct().getParent();
            return getVisualization().getFigure(parentConstruct);
        }
        return null;
    }

    public Point getPosition() {
        return this.position;
    }

    public double getRotation() {
        return this.position.getRotation();
    }

    public double getScale() {
        return this.scale;
    }

    public void setPosition(Point position) {
        this.position.set(position.getX(), position.getY());
    }

    public void setRotation(double angle) {
        this.position.setRotation(angle);
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public boolean isVisible() {
        return visibility == Visibility.VISIBLE;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void addShape(Shape shape) {
        shape.getPosition().setReferencePoint(getPosition());
        shapes.add(shape);
    }

    public void addShape(Shape shape, String label) {
        shape.setLabel(label);
        shape.getPosition().setReferencePoint(getPosition());
        shapes.add(shape);
    }

    public Shape getShape(int index) {
        return shapes.get(index);
    }

    public Shape getShape(String label) {
        for (Shape shape : shapes) {
            if (shape.getLabel().equals(label)) {
                return shape;
            }
        }
        return null;
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public Shape removeShape(int index) {
        return shapes.remove(index);
    }

    public abstract void update();

    public abstract void draw(Surface surface);

    public abstract boolean containsPoint(Point point);

    public abstract boolean containsPoint(Point point, double padding);

    public void setOnActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void processAction(Action action) {
        if (actionListener != null) {
            actionListener.onAction(action);
        }
    }

    public void setTransparency(final double transparency) {
        this.targetTransparency = transparency;

        for (int i = 0; i < shapes.size(); i++) {
            // Color
            int intColor = android.graphics.Color.parseColor(shapes.get(i).getColor());
            intColor = Color.setTransparency(intColor, this.targetTransparency);
            shapes.get(i).setColor(Color.getHexColorString(intColor));

            // Outline Color
            int outlineColorIndex = android.graphics.Color.parseColor(shapes.get(i).getOutlineColor());
            outlineColorIndex = Color.setTransparency(outlineColorIndex, this.targetTransparency);
            shapes.get(i).setOutlineColor(Color.getHexColorString(outlineColorIndex));
        }

        this.transparency = this.targetTransparency;
    }

    public List<Point> getVertices() {
        List<Point> positions = new LinkedList<>();
        for (Shape shape : shapes) {
            positions.addAll(shape.getVertices());
        }
        return positions;
    }

    public List<Point> getAbsoluteVertices() {
        List<Point> positions = new LinkedList<>();
        for (Shape shape : shapes) {
            for (Point shapeVertex : shape.getVertices()) {

                // Rotate shape about its center point
                Point absoluteVertex = Geometry.calculateRotatedPoint(shape.getPosition(), shape.getRotation(), shapeVertex);

                // Rotate shape vertices about the shape's reference point
                Point referencePoint = position;
                absoluteVertex = Geometry.calculateRotatedPoint(referencePoint, getRotation(), absoluteVertex);
                positions.add(absoluteVertex);
            }
        }
        return positions;
    }

    public Rectangle getBoundingBox() {

        List<Point> boundingBoxVertices = new LinkedList<>();

        for (int i = 0; i < shapes.size(); i++) {

            Shape shape = shapes.get(i);
            List<Point> shapeVertices = shape.getVertices();

            for (int j = 0; j < shapeVertices.size(); j++) {
                boundingBoxVertices.add(shapeVertices.get(j));
            }
        }

        return Geometry.calculateBoundingBox(boundingBoxVertices);
    }

}
