package camp.computer.clay.util.image;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import camp.computer.clay.application.graphics.Display;
import camp.computer.clay.engine.Addressable;
import camp.computer.clay.model.Entity;
import camp.computer.clay.model.Group;
import camp.computer.clay.model.action.Action;
import camp.computer.clay.model.action.ActionListener;
import camp.computer.clay.util.Color;
import camp.computer.clay.util.geometry.Geometry;
import camp.computer.clay.util.geometry.Point;
import camp.computer.clay.util.geometry.Rectangle;
import camp.computer.clay.util.image.util.ShapeGroup;

public abstract class ImageComponent<T extends Entity> extends Addressable {

    /**
     * The parent {@code Space} containing this {@code ImageComponent}.
     */
    protected Space space = null;

    protected T entity = null;

    protected List<Shape> shapes = new LinkedList<>();

    protected Point position = new Point(0, 0);

    protected double scale = 1.0;

    protected Visibility visibility = Visibility.VISIBLE;

    protected double targetTransparency = 1.0;

    protected double transparency = targetTransparency;

    protected ActionListener actionListener;

    // <LAYER>
    public static final int DEFAULT_LAYER_INDEX = 0;

    protected int layerIndex = DEFAULT_LAYER_INDEX;

    public int getLayerIndex() {
        return this.layerIndex;
    }

    public void setLayerIndex(int layerIndex) {
        this.layerIndex = layerIndex;
        space.updateLayers();
    }

    /**
     * Sorts {@code Shapes}s in the {@code ImageComponent} by layer.
     */
    public void updateLayers() {

        for (int i = 0; i < shapes.size() - 1; i++) {
            for (int j = i + 1; j < shapes.size(); j++) {
                // Check for out-of-order pairs, and swap them
                if (shapes.get(i).layerIndex > shapes.get(j).layerIndex) {
                    Shape shape = shapes.get(i);
                    shapes.set(i, shapes.get(j));
                    shapes.set(j, shape);
                }
            }
        }

        /*
        // TODO: Sort using this after making Group implement List
        Collections.sort(Database.arrayList, new Comparator<MyObject>() {
            @Override
            public int compare(MyObject o1, MyObject o2) {
                return o1.getStartDate().compareTo(o2.getStartDate());
            }
        });
        */
    }
    // </LAYER>

    /**
     * <em>Invalidates</em> the {@code Shape}. Invalidating a {@code Shape} causes its cached
     * geometry, such as its boundary, to be updated during the subsequent call to {@code update()}.
     * <p>
     * Note that a {@code Shape}'s geometry cache will only ever be updated when it is first
     * invalidated by calling {@code invalidate()}. Therefore, to cause the {@code Shape}'s
     * geometry cache to be updated, call {@code invalidate()}. The geometry cache will be updated
     * in the first call to {@code update()} following the call to {@code invalidate()}.
     */
    public void invalidate() {
        for (int i = 0; i < shapes.size(); i++) {
            shapes.get(i).invalidate();
        }
    }

    public ImageComponent(T entity) {
        this.entity = entity;
    }

    public T getEntity() {
        return this.entity;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public Space getSpace() {
        return this.space;
    }

    public Point getPosition() {
        return this.position;
    }

    public double getRotation() {
        return this.position.rotation;
    }

    public double getScale() {
        return this.scale;
    }

    public void setPosition(double x, double y) {
        position.set(x, y);
        invalidate();
    }

    public void setPosition(Point position) {
        position.set(position.x, position.y);
        invalidate();
    }

    public void setRotation(double angle) {
        this.position.rotation = angle;
        invalidate();
    }

    public void setScale(double scale) {
        this.scale = scale;
        invalidate();
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

    public <T extends Shape> void addShape(T shape) {
        shape.setImagePosition(shape.getPosition());
        shapes.add(shape);

        // Update layer ordering
        updateLayers();

        // Invalidate Shape
        shape.invalidate();
    }

    public Shape getShape(String label) {
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            if (shape.getLabel().equals(label)) {
                return shape;
            }
        }
        return null;
    }

    public Shape getShape(Entity entity) {
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            if (shape.getEntity() == entity) {
                return shape;
            }
        }
        return null;
    }

    public Shape getShape(Point point) {
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            if (shape.contains(point)) {
                return shape;
            }
        }
        return null;
    }

    public ShapeGroup getShapes() {
        ShapeGroup shapeGroup = new ShapeGroup();
        shapeGroup.addAll(this.shapes);
        return shapeGroup;
    }

    public <T extends Entity> ShapeGroup getShapes(Class<? extends Entity>... entityTypes) {
        return getShapes().filterType(entityTypes);
    }

    public <T extends Entity> ShapeGroup getShapes(Group<T> entities) {
        return getShapes().filterEntity(entities);
    }

    /**
     * Removes elements <em>that do not match</em> the regular expressions defined in
     * {@code labels}.
     *
     * @param labelPatterns The list of {@code Shape} objects matching the regular expressions list.
     * @return A list of {@code Shape} objects.
     */
    public ShapeGroup getShapes(String... labelPatterns) {

        ShapeGroup shapeGroup = new ShapeGroup();

        for (int i = 0; i < this.shapes.size(); i++) {
            for (int j = 0; j < labelPatterns.length; j++) {

                Pattern pattern = Pattern.compile(labelPatterns[j]);
                Matcher matcher = pattern.matcher(this.shapes.get(i).getLabel());

                if (matcher.matches()) {
                    shapeGroup.add(this.shapes.get(i));
                }
            }
        }

        return shapeGroup;
    }

    public Shape removeShape(int index) {
        return shapes.remove(index);
    }

    public void update() {
        updateGeometry();
    }

    protected void updateGeometry() {

        // Update Shapes
        for (int i = 0; i < this.shapes.size(); i++) {
            Shape shape = this.shapes.get(i);

            // Update the Shape
            shape.update(position);
        }
    }

    public abstract void draw(Display display);

    /**
     * Returns {@code true} if any of the {@code Shape}s in the {@code ImageComponent} contain the
     * {@code point}.
     *
     * @param point
     * @return
     */
    public boolean contains(Point point) {
        if (isVisible()) {
            for (int i = 0; i < shapes.size(); i++) {
                if (shapes.get(i).contains(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setOnActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void processAction(Action action) {
        if (actionListener != null) {
            actionListener.onAction(action);
        }
    }

    // TODO: Delete?
    public void setTransparency(final double transparency) {
        this.targetTransparency = transparency;

        for (int i = 0; i < shapes.size(); i++) {

            Shape shape = shapes.get(i);

            // Color
            int intColor = android.graphics.Color.parseColor(shapes.get(i).getColor());
            intColor = Color.setTransparency(intColor, this.targetTransparency);
            shape.setColor(Color.getHexColorString(intColor));

            // Outline Color
            int outlineColorIndex = android.graphics.Color.parseColor(shapes.get(i).getOutlineColor());
            outlineColorIndex = Color.setTransparency(outlineColorIndex, this.targetTransparency);
            shape.setOutlineColor(Color.getHexColorString(outlineColorIndex));
        }

        this.transparency = this.targetTransparency;
    }

    public Rectangle getBoundingBox() {
        List<Point> shapeBoundaries = new LinkedList<>();
        for (int i = 0; i < shapes.size(); i++) {
            shapeBoundaries.addAll(shapes.get(i).getBoundary());
        }
        return Geometry.getBoundingBox(shapeBoundaries);
    }

    // TODO: Delete!
    public double getRelativeAngle(Point point) {

        double relativeAngle = Geometry.getAngle(position, point);
        if (relativeAngle < 0) {
            relativeAngle += 360.0;
        }
        relativeAngle = (relativeAngle - getRotation());
        if (relativeAngle < 0) {
            relativeAngle += 360.0;
        }
        return relativeAngle;
    }

//    public Point getPoint(double x, double y) {
//        Point point = new Point();
//
//        double x2 = Geometry.distance(0, 0, x, y) * Math.cos(Math.toRadians(position.rotation + Geometry.getAngle(0, 0, x, y)));
//        point.x = position.x + x2;
//
//        double y2 = Geometry.distance(0, 0, x, y) * Math.sin(Math.toRadians(position.rotation + Geometry.getAngle(0, 0, x, y)));
//        point.y = position.y + y2;
//
//        return point;
//    }
}