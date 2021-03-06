package camp.computer.clay.engine.system;

import camp.computer.clay.engine.World;
import camp.computer.clay.engine.component.Model;
import camp.computer.clay.engine.component.Path;
import camp.computer.clay.engine.component.Transform;
import camp.computer.clay.engine.component.TransformConstraint;
import camp.computer.clay.engine.component.util.FilterStrategy;
import camp.computer.clay.engine.entity.Entity;
import camp.computer.clay.engine.manager.Group;
import camp.computer.clay.util.Geometry;

public class ModelSystem extends System {

    private Group<Entity> entities;

    public ModelSystem(World world) {
        super(world);
        setup();
    }

    private void setup() {
        entities = world.entityManager.subscribe(
                new FilterStrategy(Group.Filters.filterWithComponents, Model.class, Transform.class)
        );
    }

    @Override
    public void update(long dt) {
        for (int i = 0; i < entities.size(); i++) {
            updateTransform(entities.get(i));
        }
    }

    // Previously: ModelBuilder.update()
    // Required Components: ModelBuilder, Transform
    private void updateTransform(Entity entity) {

        // Start by transforming base images. They will never have more than one level of constraints (?).
        Transform absoluteReferenceTransform = null;
        if (entity.hasComponent(TransformConstraint.class)) {
            // <HACK>
            TransformConstraint layoutConstraint = entity.getComponent(TransformConstraint.class);
            Transform referenceTransform = layoutConstraint.getReferenceEntity().getComponent(Transform.class);
            Transform relativeTransform = layoutConstraint.relativeTransform;

            absoluteReferenceTransform = new Transform();
            absoluteReferenceTransform.x = referenceTransform.x + Geometry.distance(0, 0, relativeTransform.x, relativeTransform.y) * Math.cos(Math.toRadians(referenceTransform.rotation + Geometry.getAngle(0, 0, relativeTransform.x, relativeTransform.y)));
            absoluteReferenceTransform.y = referenceTransform.y + Geometry.distance(0, 0, relativeTransform.x, relativeTransform.y) * Math.sin(Math.toRadians(referenceTransform.rotation + Geometry.getAngle(0, 0, relativeTransform.x, relativeTransform.y)));
            // </HACK>
        } else {

            // HACK!
            // TODO: Remove this. Shouldn't need this in addition to the previous block in this condition... i.e., paths shouldn't be a special case! Generalize handling EDIT state (or make it not important)
            absoluteReferenceTransform = entity.getComponent(Transform.class);
        }

        // Update Shapes
        if (entity.hasComponent(Path.class)) {
            // TODO: Refactor so a special case isn't needed for Path.
        } else {
            Group<Entity> primitives = entity.getComponent(Model.class).primitives;
            for (int i = 0; i < primitives.size(); i++) {
                if (absoluteReferenceTransform != null) {
                    // TODO: if (shape.hasComponent(TransformConstraint.class)) {
                    updateRelativeTransform(primitives.get(i), absoluteReferenceTransform);
                }
            }
        }
    }

    /**
     * Computes and updates the {@code Shape}'s absolute positioning, rotation, and scaling in
     * preparation for drawing and collision detection.
     * <p>
     * Updates the x and y coordinates of {@code Shape} relative to this {@code ModelBuilder}. Translate
     * the center position of the {@code Shape}. Effectively, this updates the position of the
     * {@code Shape}.
     *
     * @param referenceTransform Position of the containing {@code ModelBuilder} relative to which the
     *                           {@code Shape} will be drawn.
     */
    private void updateRelativeTransform(Entity primitive, Transform referenceTransform) { // previously updateShapeRelativeTransform

        TransformConstraint transformConstraint = primitive.getComponent(TransformConstraint.class);

        // Position
        double distanceToRelativeTransform = Geometry.distance(0, 0, transformConstraint.relativeTransform.x, transformConstraint.relativeTransform.y);
        double angle = Geometry.getAngle(0, 0, transformConstraint.relativeTransform.x, transformConstraint.relativeTransform.y);

        primitive.getComponent(Transform.class).x = referenceTransform.x + distanceToRelativeTransform * Math.cos(Math.toRadians(referenceTransform.rotation + angle));
        primitive.getComponent(Transform.class).y = referenceTransform.y + distanceToRelativeTransform * Math.sin(Math.toRadians(referenceTransform.rotation + angle));

        // Rotation
        primitive.getComponent(Transform.class).rotation = referenceTransform.rotation + transformConstraint.relativeTransform.rotation;
    }
}
