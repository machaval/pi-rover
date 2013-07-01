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
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

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

        final Timer timer = new Timer();
        final GpioPinListenerDigital listenerDigital = new GpioPinListenerDigital()
        {
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
            {
                if (event.getState().isHigh())
                {
                    timer.start();
                }
                else
                {
                    timer.end();
                }

            }
        };
        echo.addListener(listenerDigital);
        trigger.pulse(2, true);
        double result = (timer.timeNano() * 0.0000343) / 2; //time multiplied by sound distance  divided by two because is go and return
        echo.removeListener(listenerDigital);
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
