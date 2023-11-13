package com.nosenkomi.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Bucket  {
    public Fixture top;
    public Fixture bottom;

    public Body body;

    public Bucket(World world, float x, float y, int width, int height){
        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / Constants.PPM, y / Constants.PPM);
        bodyDef.fixedRotation = true; // prevent from rotation

        // create body in referenced world
        body = world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 / Constants.PPM, height / 2 / Constants.PPM);

        FixtureDef bottomDef = new FixtureDef();
        bottomDef.density = 1;
        bottomDef.shape = shape;

        bottom = body.createFixture(bottomDef);

        PolygonShape topShape = new PolygonShape();
        topShape.setAsBox(
                width / 2 / Constants.PPM, 1 / Constants.PPM,
                new Vector2(body.getLocalCenter().x, body.getLocalCenter().y + (height / 2 / Constants.PPM)),
                0);

        FixtureDef topDef = new FixtureDef();
        topDef.density = 1;
        topDef.shape = topShape;

        top = body.createFixture(topDef);

        shape.dispose();
        topShape.dispose();
    }

    public float getX(){
        return body.getPosition().x;
    }

    public float getY(){
        return body.getPosition().y;
    }
}
