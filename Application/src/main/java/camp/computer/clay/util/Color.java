package camp.computer.clay.util;

import java.util.HashMap;
import java.util.Random;

import camp.computer.clay.engine.component.Path;
import camp.computer.clay.engine.component.Port;
import camp.computer.clay.engine.entity.Entity;

public abstract class Color {

    public static String GREEN_POLOLU = "#FF336633"; // web-safe from #347A1F
    public static String RED_SPARKFUN = "#FFE62C2E"; // Source: https://learn.sparkfun.com/tutorials/make-your-own-fritzing-parts
    public static String PURPLE_LILYPAD = "#FF582E67";
    public static String BLUE_ARDUINO = "#FF91730F";
    public static String ORANGE = "#FFFF4500";
    public static String BLACK = "#FF1D1A1C";
    public static String YELLOW = "#FFFFE512";
    public static String WHITE_BREADBOARD = "#FFD9D9D9";

    public static String RED_BREADBOARD = "#5E5EEE";
    public static String BLUE_BREADBOARd = "#EE5E5E";

    // <TEMPORARY>
    public static String getRandomBoardColor() {
        int randomIndex = camp.computer.clay.util.Random.generateRandomInteger(0, 8);
        String colors[] = {
                GREEN_POLOLU,
                RED_SPARKFUN,
                PURPLE_LILYPAD,
                BLUE_ARDUINO,
                ORANGE,
                BLACK,
                YELLOW,
                WHITE_BREADBOARD
        };
        return colors[randomIndex];
    }
    // </TEMPORARY>

    public static int[] PATH_COLOR_PALETTE = new int[]{
            android.graphics.Color.parseColor("#19B5FE"),
            android.graphics.Color.parseColor("#2ECC71"),
            android.graphics.Color.parseColor("#F22613"),
            android.graphics.Color.parseColor("#F9690E"),
            android.graphics.Color.parseColor("#9A12B3"),
            android.graphics.Color.parseColor("#F9BF3B"),
            android.graphics.Color.parseColor("#DB0A5B"),
            android.graphics.Color.parseColor("#BF55EC"),
            android.graphics.Color.parseColor("#A2DED0"),
            android.graphics.Color.parseColor("#1E8BC3"),
            android.graphics.Color.parseColor("#36D7B7"),
            android.graphics.Color.parseColor("#EC644B")
    };
    private static HashMap<Entity, Integer> colorMap = new HashMap<>();

    public static int getUniqueColor(Entity entity) {

        if (colorMap.containsKey(entity)) {
            return colorMap.get(entity);
        }

        for (int i = 0; i < PATH_COLOR_PALETTE.length; i++) {
            if (!colorMap.containsValue(PATH_COLOR_PALETTE[i])) {
                colorMap.put(entity, PATH_COLOR_PALETTE[i]);
                return PATH_COLOR_PALETTE[i];
            }
        }

        Random random = new Random();
        while (true) {
            int red = 30 + random.nextInt(225);
            int green = 30 + random.nextInt(225);
            int blue = 30 + random.nextInt(225);
            int randomColor = android.graphics.Color.rgb(red, green, blue);
            if (!colorMap.containsValue(randomColor)) {
                colorMap.put(entity, randomColor);
                return randomColor;
            }
        }
    }

    public static int setTransparency(int color, double factor) {
        int alpha = (int) (255.0 * factor); // Math.round(android.graphics.Color.alpha(color) * (float) factor);
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, red, green, blue);
    }

    public static String getHexColorString(int color) {
        return String.format("#%08X", (0xFFFFFFFF & color));
    }

    // <HACK>
    // Color palette generated with i want hue.
    // Reference: http://tools.medialab.sciences-po.fr/iwanthue/index.php
    public static String PORT_COLOR_OFF = "#ffefefef";
    public static String PORT_COLOR_SWITCH = "#ff4CA73D"; // Greens: #6aa84f
    public static String PORT_COLOR_PULSE = "#ffF9E805"; // Yellows: #ff9900, #E5C700
    public static String PORT_COLOR_WAVE = "#ff0070FF"; // Blues: #3c78d8
    public static String PORT_COLOR_REFERENCE = "#ff883C00"; // Blacks: #0D1410, Browns: #883C00, #783f04
    public static String PORT_COLOR_CMOS = "#ffFF4300"; // Reds: #cc0000, #E41900
    public static String PORT_COLOR_TTL = "#ffFF1493";

    public static String getColor(Port.Type portType) {
        if (portType == Port.Type.NONE) {
            return PORT_COLOR_OFF;
        } else if (portType == Port.Type.SWITCH) {
            return PORT_COLOR_SWITCH;
        } else if (portType == Port.Type.PULSE) {
            return PORT_COLOR_PULSE;
        } else if (portType == Port.Type.WAVE) {
            return PORT_COLOR_WAVE;
        } else if (portType == Port.Type.POWER_REFERENCE) {
            return PORT_COLOR_REFERENCE;
        } else if (portType == Port.Type.POWER_CMOS) {
            return PORT_COLOR_CMOS;
        } else if (portType == Port.Type.POWER_TTL) {
            return PORT_COLOR_TTL;
        } else {
            return PORT_COLOR_OFF;
        }
    }

    public static String getColor(Path.Type pathType) {
        if (pathType == Path.Type.NONE) {
            return PORT_COLOR_OFF;
        } else if (pathType == Path.Type.SWITCH) {
            return PORT_COLOR_SWITCH;
        } else if (pathType == Path.Type.PULSE) {
            return PORT_COLOR_PULSE;
        } else if (pathType == Path.Type.WAVE) {
            return PORT_COLOR_WAVE;
        } else if (pathType == Path.Type.POWER_REFERENCE) {
            return PORT_COLOR_REFERENCE;
        } else if (pathType == Path.Type.POWER_CMOS) {
            return PORT_COLOR_CMOS;
        } else if (pathType == Path.Type.POWER_TTL) {
            return PORT_COLOR_TTL;
        } else {
            return PORT_COLOR_OFF;
        }
    }
    // </HACK>
}
