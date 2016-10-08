package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;

import static microtrafficsim.math.MathUtils.clamp;


/**
 * Tile intersection tests for various geometry.
 *
 * @author Maximilian Luz
 */
public class TileIntersectors {
    private TileIntersectors() {}


    /**
     * Tests the given point projected using the given projection for intersection against the given tile.
     *
     * @param point      the point to test for intersection.
     * @param tile       the tile to test the point against.
     * @param projection the projection used to project the given point.
     * @return {@code true} if the projected point intersects with the given tile.
     */
    public static boolean intersect(Point point, Rect2d tile, Projection projection) {
        Vec2d c = projection.project(point.coordinate);
        return c.x >= tile.xmin && c.x <= tile.xmax && c.y >= tile.ymin && c.y <= tile.ymax;
    }

    /**
     * Tests the given line projected using the given projection for intersection against the given tile.
     *
     * @param line       the line to test for intersection.
     * @param tile       the tile to test the line against.
     * @param projection the projection used to project the given line.
     * @return {@code true} if the projected line intersects with the given tile.
     */
    public static boolean intersect(MultiLine line, Rect2d tile, Projection projection) {

        Vec2d a = projection.project(line.coordinates[0]);
        for (int i = 1; i < line.coordinates.length; i++) {
            Vec2d b = projection.project(line.coordinates[i]);

            // if completely out of bounds, continue
            if ((a.x < tile.xmin && b.x < tile.xmin) || (a.x > tile.xmax && b.x > tile.xmax)) continue;
            if ((a.y < tile.ymin && b.y < tile.ymin) || (a.y > tile.ymax && b.y > tile.ymax)) continue;

            // clamp line-segment to tile
            double cxa = clamp(a.x, tile.xmin, tile.xmax);
            double cxb = clamp(b.x, tile.xmin, tile.xmax);
            double cya = clamp(a.y, tile.ymin, tile.ymax);
            double cyb = clamp(b.y, tile.ymin, tile.ymax);

            // transform the clamped coordinates to line coordinates
            double sxa = (cxa - a.x) / (b.x - a.x);
            double sxb = (cxb - a.x) / (b.x - a.x);
            double sya = (cya - a.y) / (b.y - a.y);
            double syb = (cyb - a.y) / (b.y - a.y);

            // check if line-segments intersect
            if (sxb >= sxa && syb >= sya) return true;

            a = b;
        }

        return false;
    }

    /**
     * Tests the given polygon projected using the given projection for intersection against the given tile.
     *
     * @param polygon    the polygon to test for intersection.
     * @param tile       the tile to test the polygon against.
     * @param projection the projection used to project the given polygon.
     * @return {@code true} if the projected polygon intersects with the given tile.
     */
    public static boolean intersect(Polygon polygon, Rect2d tile, Projection projection) {
        boolean x = false;      // x-axis coverage flag
        boolean y = false;      // y-axis coverage flag

        Vec2d a = projection.project(polygon.outline[0]);
        for (int i = 1; i < polygon.outline.length; i++) {
            Vec2d b = projection.project(polygon.outline[i]);

            // if completely out of bounds, continue
            if ((a.x < tile.xmin && b.x < tile.xmin) || (a.x > tile.xmax && b.x > tile.xmax)) continue;
            if ((a.y < tile.ymin && b.y < tile.ymin) || (a.y > tile.ymax && b.y > tile.ymax)) continue;

            // clamp outline-segment to tile
            double cxa = clamp(a.x, tile.xmin, tile.xmax);
            double cxb = clamp(b.x, tile.xmin, tile.xmax);
            double cya = clamp(a.y, tile.ymin, tile.ymax);
            double cyb = clamp(b.y, tile.ymin, tile.ymax);

            // transform the clamped coordinates to line coordinates
            double sxa = (cxa - a.x) / (b.x - a.x);
            double sxb = (cxb - a.x) / (b.x - a.x);
            double sya = (cya - a.y) / (b.y - a.y);
            double syb = (cyb - a.y) / (b.y - a.y);

            // set x/y-axis coverage flags
            x |= sxb >= sxa;
            y |= syb >= sya;

            // if the outline covers both the x- and y-axis of the rectangle, the polygon intersects the rectangle.
            if (x && y) return true;

            a = b;
        }

        return false;
    }
}
