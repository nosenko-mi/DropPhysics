package com.nosenkomi.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class B2DBodyConstructor {

    public static Body createBucket(World world, float x, float y, int width, int height){
        Body body;

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / Constants.PPM, y / Constants.PPM);
        bodyDef.fixedRotation = true; // prevent from rotation

        // create body in referenced world
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 / Constants.PPM, height / 2 / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.shape = shape;

        body.createFixture(fixtureDef);

        PolygonShape topShape = new PolygonShape();
        topShape.setAsBox(
                width / 2 / Constants.PPM, 1 / Constants.PPM,
                new Vector2(body.getLocalCenter().x, body.getLocalCenter().y + (height / 2 / Constants.PPM)),
                0);

        FixtureDef fixtureDefTop = new FixtureDef();
        fixtureDefTop.density = 1;
        fixtureDefTop.shape = topShape;

        body.createFixture(fixtureDefTop);

        shape.dispose();
        topShape.dispose();

        // return ref to body
        return body;
    }

    public static Body createBox(World world, float x, float y, int width, int height, boolean isStatic) {
        Body body;

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / Constants.PPM, y / Constants.PPM);
        bodyDef.fixedRotation = true; // prevent from rotation

        // create body in referenced world
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 / Constants.PPM, height / 2 / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.shape = shape;
//        fixtureDef.friction = 0.5f;
//        fixtureDef.restitution = 0.3f;

        body.createFixture(fixtureDef);

        shape.dispose();
        // return ref to body
        return body;

    }

    public static Body createSensor(World world, float x, float y, int width, int height, boolean isStatic) {
        Body body;
        BodyDef bodyDef = new BodyDef();

        bodyDef.type = isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / Constants.PPM, y / Constants.PPM);
        bodyDef.fixedRotation = true; // prevent from rotation

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 / Constants.PPM, height / 2 / Constants.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.isSensor = true;
        fixtureDef.shape = shape;

        body.createFixture(fixtureDef);

        shape.dispose();
        return body;
    }

}
