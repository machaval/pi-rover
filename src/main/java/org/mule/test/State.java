package org.mule.test;

/**
 *
 */
public enum State
{
    FORWARD
            {
                @Override
                public void execute(Rover rover)
                {
                    rover.forward();
                }
            }, LEFT
        {
            @Override
            public void execute(Rover rover)
            {
                rover.rotateLeft();
            }
        }, RIGHT
        {
            @Override
            public void execute(Rover rover)
            {
                rover.rotateRight();
            }
        }, BACKWARDS
        {
            @Override
            public void execute(Rover rover)
            {
                rover.backwards();
            }
        }, STOPPED
        {
            @Override
            public void execute(Rover rover)
            {
                rover.stop();
            }
        };

    public abstract void execute(Rover rover);
}
