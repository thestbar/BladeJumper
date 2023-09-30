package com.thestbar.ludumdare54.utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class TiledObjectUtil {
    public static void parseTiledObjectLayer(World world, MapObjects objects) {
        Shape shape;
        for (MapObject object : objects) {
            System.out.println(object.getClass());
            if (object instanceof PolylineMapObject) {
                shape = createPolyline((PolylineMapObject) object);
            } else if (object instanceof PolygonMapObject) {
                shape = createPolygon((PolygonMapObject) object);
            }
            else {
                continue;
            }
            Body body;
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            body = world.createBody(bodyDef);
            body.createFixture(shape, 1.0f);
            assert shape != null;
            shape.dispose();
        }
    }

    private static ChainShape createPolyline(PolylineMapObject polylineMapObject) {
        float[] vertices = polylineMapObject.getPolyline().getTransformedVertices();
        int len = vertices.length / 2;
        Vector2[] worldVertices = new Vector2[len];
        for (int i = 0; i < len; ++i) {
            worldVertices[i] = new Vector2(vertices[i * 2] / Constants.PPM, vertices[i * 2 + 1] / Constants.PPM);
        }
        ChainShape chainShape = new ChainShape();
        chainShape.createChain(worldVertices);
        return chainShape;
    }

    private static ChainShape createPolygon(PolygonMapObject polygonMapObject) {
        float[] vertices = polygonMapObject.getPolygon().getTransformedVertices();
        int len = vertices.length / 2;
        Vector2[] worldVertices = new Vector2[len];
        for (int i = 0; i < len; ++i) {
            worldVertices[i] = new Vector2(vertices[i * 2] / Constants.PPM, vertices[i * 2 + 1] / Constants.PPM);
        }
        ChainShape chainShape = new ChainShape();
        chainShape.createChain(worldVertices);
        return chainShape;
    }

    private static ChainShape createRectangle(RectangleMapObject rectangleMapObject) {
        Rectangle rectangle = rectangleMapObject.getRectangle();
        Vector2[] rectangleVertices = new Vector2[4];
        // Top left
        rectangleVertices[0] = new Vector2(rectangle.x, rectangle.y + rectangle.height);
        // Top right
        rectangleVertices[1] = new Vector2(rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        // Bottom left
        rectangleVertices[2] = new Vector2(rectangle.x, rectangle.y);
        // Bottom right
        rectangleVertices[3] = new Vector2(rectangle.x + rectangle.width, rectangle.y);
        ChainShape chainShape = new ChainShape();
        chainShape.createChain(rectangleVertices);
        return chainShape;
    }
}
