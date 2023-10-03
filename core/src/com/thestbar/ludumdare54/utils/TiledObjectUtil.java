package com.thestbar.ludumdare54.utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.Arrays;

public class TiledObjectUtil {
    public static void parseTiledObjectLayer(World world, MapObjects objects) {
        Shape shape;
        for (MapObject object : objects) {
            Body body;
            BodyDef def = new BodyDef();
            def.type = BodyDef.BodyType.StaticBody;
            def.fixedRotation = true;
            def.linearDamping = 2f;
            body = world.createBody(def);
            if (object instanceof PolylineMapObject) {
                shape = createPolyline((PolylineMapObject) object);
            } else if (object instanceof RectangleMapObject) {
                shape = createRectangle((RectangleMapObject) object);
            } else {
                continue;
            }

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
            shape.dispose();

            body.getFixtureList().get(0).setDensity(1);
            body.getFixtureList().get(0).setFriction(0);
            body.getFixtureList().get(0).getFilterData().categoryBits = Constants.BIT_GROUND;
            body.getFixtureList().get(0).getFilterData().maskBits = Constants.BIT_PLAYER | Constants.BIT_GROUND_SENSOR | Constants.BIT_ENEMY | Constants.BIT_BULLET;
            body.getFixtureList().get(0).setUserData("ground");
        }
    }

    public static ChainShape createPolyline(PolylineMapObject polylineMapObject) {
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

//    public static PolygonShape createPolygon(PolygonMapObject polygonMapObject) {
//        System.out.println("Hello");
//        float[] vertices = polygonMapObject.getPolygon().getTransformedVertices();
//        int len = vertices.length;
//        for (int i = 0; i < len; ++i) {
//            vertices[i] /= Constants.PPM;
//        }
//        PolygonShape polygonShape = new PolygonShape();
//        polygonShape.set(vertices);
//        return polygonShape;
//    }

    private static ChainShape createRectangle(RectangleMapObject rectangleMapObject) {
        Rectangle rectangle = rectangleMapObject.getRectangle();
        Vector2[] rectangleVertices = new Vector2[4];
        // Top left
        rectangleVertices[0] = new Vector2(rectangle.x / Constants.PPM, rectangle.y / Constants.PPM + 1);
        // Top right
        rectangleVertices[1] = new Vector2(rectangle.x / Constants.PPM + 1, rectangle.y  / Constants.PPM + 1);
        // Bottom right
        rectangleVertices[2] = new Vector2(rectangle.x / Constants.PPM +  1, rectangle.y / Constants.PPM);
        // Bottom left
        rectangleVertices[3] = new Vector2(rectangle.x / Constants.PPM, rectangle.y / Constants.PPM);
        ChainShape chainShape = new ChainShape();
        chainShape.createChain(rectangleVertices);
        return chainShape;
    }
}
