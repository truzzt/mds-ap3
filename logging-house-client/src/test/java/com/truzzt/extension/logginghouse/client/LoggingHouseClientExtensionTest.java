import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.truzzt.extension.logginghouse.client.LoggingHouseClientExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingHouseClientExtensionTest {

    private LoggingHouseClientExtension extension;

    @Mock
    private ServiceExtensionContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extension = new LoggingHouseClientExtension();
    }

    @Test
    void name_shouldReturnCorrectName() {
        assertEquals(LoggingHouseClientExtension.LOGGINGHOUSE_CLIENT_EXTENSION, extension.name());
    }
}
