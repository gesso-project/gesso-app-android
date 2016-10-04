package camp.computer.clay.model;

import camp.computer.clay.model.profile.PortableProfile;

/**
 * {@code Extension} represents an <em>extension</em> to a {@code PhoneHost}.
 */
public class Extension extends Portable {
    // Servo: GND, 3.3V, PWM
    // DC Motor: GND, 5V
    // IR Rangefinder: GND, 3.3V, Signal (analog)
    // Potentiometer: GND, 3.3V, Signal (analog)

    public Extension() {
        super();
    }

    public Extension(PortableProfile portableProfile) {
        super(portableProfile);
    }
}