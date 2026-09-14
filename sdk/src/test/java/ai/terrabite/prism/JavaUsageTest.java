package ai.terrabite.prism;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

/**
 * Compiles the README's Java usage against the real API. Exists to catch a
 * missing {@code @JvmStatic} or a listener that Java cannot pass as a lambda,
 * both of which Kotlin callers never notice.
 */
public class JavaUsageTest {

    @After
    public void clear() {
        Prism.setLocationListener(null);
        Prism.setErrorListener(null);
    }

    @Test
    public void listenersAreLambdasAndConfigHasABuilder() {
        final PrismLocation[] received = new PrismLocation[1];
        Prism.setLocationListener(location -> received[0] = location);
        Prism.setErrorListener(error -> { });

        PrismConfig config = new PrismConfig.Builder()
                .setTrackingMode(PrismTrackingMode.STANDARD)
                .setHorizontalAccuracyThreshold(50)
                .setNotificationTitle("Delivering")
                .build();
        assertEquals(PrismTrackingMode.STANDARD, config.getTrackingMode());
        assertEquals(50, config.getHorizontalAccuracyThreshold());

        PrismLocation location = LocationFixtures.prism();
        PrismDispatcher.INSTANCE.deliver(location);
        assertEquals(location, received[0]);
        assertEquals("loc-0001", received[0].getId());
        assertEquals(1000, Prism.LOCATION_PERMISSION_REQUEST_CODE);
    }
}
