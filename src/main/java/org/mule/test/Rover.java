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

import java.util.HashMap;
import java.util.Map;

public class Rover
{


    public static final int THRESHOLD = 2;
    public static final int NUMBER_SAMPLES = 10;
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

    private void waitNS(long waitTime)
    {
        long start = System.nanoTime();
        long end;
        do
        {
            end = System.nanoTime();
        }
        while (start + waitTime >= end);
    }

    public double measureDistance()
    {

        double[] values = new double[NUMBER_SAMPLES];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = doMeasureDistance();
        }
        return getMode(values); //Calculate the mode
    }

    private double getMode(double[] values)
    {
        final HashMap<Double, Integer> freqs = new HashMap<Double, Integer>();

        for (double val : values)
        {
            boolean updated = false;
            for (Double key : freqs.keySet())
            {
                if (Math.abs(key - val) < THRESHOLD)
                {
                    Integer integer = freqs.get(key);
                    freqs.put(key, integer + 1);
                    updated = true;
                }
            }
            if (!updated)
            {
                freqs.put(val, 1);
            }
        }

        double mode = 0;
        int maxFreq = 0;

        for (Map.Entry<Double, Integer> entry : freqs.entrySet())
        {
            int freq = entry.getValue();
            if (freq > maxFreq)
            {
                maxFreq = freq;
                mode = entry.getKey();
            }
        }

        return mode;
    }

    private double doMeasureDistance()
    {
        long start = 0;
        long end = 0;
        trigger.setState(PinState.HIGH);
        waitNS(20L);
        trigger.setState(PinState.LOW);

        long startMethod = System.nanoTime();

        while (echo.isLow())
        {
            start = System.nanoTime();
            if ((startMethod - start) > 5000000)  //Bigger than 5 milliseconds return
            {
                return -1;
            }
        }
        while (echo.isHigh())
        {
            end = System.nanoTime();
            if ((startMethod - end) > 20000000)   //Bigger than 20 milliseconds return
            {
                return -1;
            }
        }

        long nanoTime = end - start;
        System.out.println("nanoTime = " + nanoTime);
        return (nanoTime * (0.0000343)) / 2;
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
