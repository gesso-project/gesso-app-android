package camp.computer.clay.application;

import camp.computer.clay.visualization.util.Time;

/**
 * VisualizationRenderer is a background thread that periodically updates the visualization state
 * and renders it. By default, the renderer targets frames per second, each time advancing the
 * visualization's state then re-rendering it.
 */
public class VisualizationRenderer extends Thread {

    // <SETTINGS>
    final public static int DEFAULT_TARGET_FRAMES_PER_SECOND = 30;

    public static boolean ENABLE_THREAD_SLEEP = true;

    public static boolean ENABLE_STATISTICS = true;

    private int targetFramesPerSecond = DEFAULT_TARGET_FRAMES_PER_SECOND;
    // </SETTINGS>

    private VisualizationSurface visualizationSurface;

    private boolean isRunning = false;

    VisualizationRenderer(VisualizationSurface visualizationSurface) {
        super();
        this.visualizationSurface = visualizationSurface;
    }

    public void setRunning (boolean isRunning) {
        this.isRunning = isRunning;
    }

    // <STATISTICS>
    private double currentFramesPerSecond = 0;
    private int fpsSampleIndex = 0;
    private final int fpsSampleLimit = targetFramesPerSecond; // Moving FPS average for last second.
    private double[] fpsSamples = new double[fpsSampleLimit];
    // </STATISTICS>

    @Override
    public void run () {

        long framePeriod = 1000 / targetFramesPerSecond; // Frame period in milliseconds
        long frameStartTime;
        long frameStopTime;
        long frameSleepTime;

        while (isRunning) {

            frameStartTime = Time.getCurrentTime();

            // Advance the visualization state
            visualizationSurface.update();

            frameStopTime = Time.getCurrentTime();

            if (ENABLE_STATISTICS) {
                // Store actual frames per second
                currentFramesPerSecond = (1000.0f / (float) (frameStopTime - frameStartTime));

                // Store moving average
                fpsSamples[fpsSampleIndex] = currentFramesPerSecond;
                fpsSampleIndex = (fpsSampleIndex + 1) % fpsSampleLimit;
            }

            // Sleep the thread until the time remaining in the frame's allocated draw time expires.
            // This reduces energy consumption thereby increasing battery life.
            if (ENABLE_THREAD_SLEEP) {
                frameSleepTime = framePeriod - (frameStopTime - frameStartTime);
                try {
                    if (frameSleepTime > 0) {
                        Thread.sleep(frameSleepTime);
                    }
                } catch (Exception e) {
                }
            }

        }
    }

    public long getTargetFramesPerSecond () {
        return targetFramesPerSecond;
    }

    public void setTargetFramesPerSecond (int framesPerSecond) {
        this.targetFramesPerSecond = framesPerSecond;
    }

    public double getFramesPerSecond() {

        double fpsTotal = 0;

        for (int i = 0; i < fpsSampleLimit; i++) {
            fpsTotal = fpsTotal + fpsSamples[i];
        }

        return (fpsTotal / fpsSampleLimit);
    }
}
