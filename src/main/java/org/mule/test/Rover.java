/**
 *
 */
package org.mule.test;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Rover
{


    private GpioController gpio;
    private GpioPinDigitalOutput goLeftEngine;
    private GpioPinDigitalOutput goRightEngine;
    private GpioPinDigitalOutput backLeftEngine;
    private GpioPinDigitalOutput backRightEngine;


    private State currentState;
    private GpioPinDigitalOutput trigger;
    private GpioPinDigitalInput echo;


    public void initialize()
    {
        System.out.println("Starting rover");

        // create gpio controller
        gpio = GpioFactory.getInstance();
        // provision gpio pin #01 as an output pin and turn on

        goLeftEngine = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "GoLeft", PinState.LOW);
        goRightEngine = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "GoRight", PinState.LOW);
        backLeftEngine = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "BackLeft", PinState.LOW);
        backRightEngine = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "BackRight", PinState.LOW);

        trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "Trigger", PinState.LOW);
        echo = gpio.provisionDigitalInputPin(RaspiPin.GPIO_06, "Echo");


        stop();
    }

    public RoverInfo getInfo()
    {
        return new RoverInfo();
    }

    public double measureDistance()
    {

        int stateIndex = 0;
        long start = 0;
        long end = 0;
        trigger.setState(PinState.HIGH);
        trigger.setState(PinState.LOW);


        while (stateIndex < 2)
        {

            if (echo.isHigh())
            {
                if (stateIndex == 0)
                {
                    start = System.nanoTime();
                    stateIndex++;
                }
                else
                {
                    continue;
                }
            }
            else
            {
                if (stateIndex == 1)
                {
                    end = System.nanoTime();
                    stateIndex++;
                }
                else
                {
                    return -1;
                }
            }
        }

        long nanoTime = end - start;
        double result = (nanoTime * 0.0000343);
        return result;
    }

    public void move(String direction)
    {
        State state = State.valueOf(direction);
        state.execute(this);
    }


    public void forward()
    {
        //Left Part
        goLeftEngine.setState(PinState.HIGH);
        backLeftEngine.setState(PinState.LOW);

        //Right Part
        goRightEngine.setState(PinState.HIGH);
        backRightEngine.setState(PinState.LOW);

        setCurrentState(State.FORWARD);

    }

    public void rotateLeft()
    {
        goLeftEngine.setState(PinState.LOW);
        backLeftEngine.setState(PinState.HIGH);


        goRightEngine.setState(PinState.HIGH);
        backRightEngine.setState(PinState.LOW);
        setCurrentState(State.LEFT);
    }

    public void rotateRight()
    {
        goLeftEngine.setState(PinState.HIGH);
        backLeftEngine.setState(PinState.LOW);

        goRightEngine.setState(PinState.LOW);
        backRightEngine.setState(PinState.HIGH);
        setCurrentState(State.RIGHT);
    }

    public void backwards()
    {
        goLeftEngine.setState(PinState.LOW);
        backLeftEngine.setState(PinState.HIGH);

        goRightEngine.setState(PinState.LOW);
        backRightEngine.setState(PinState.HIGH);
        setCurrentState(State.BACKWARDS);
    }


    public void stop()
    {
        goLeftEngine.setState(PinState.LOW);
        backLeftEngine.setState(PinState.LOW);

        goRightEngine.setState(PinState.LOW);
        backRightEngine.setState(PinState.LOW);

        setCurrentState(State.STOPPED);

    }


    public State getCurrentState()
    {
        return currentState;
    }


    public void end()
    {
        stop();
        gpio.shutdown();
    }

    public void setCurrentState(State currentState)
    {
        this.currentState = currentState;
    }
}
