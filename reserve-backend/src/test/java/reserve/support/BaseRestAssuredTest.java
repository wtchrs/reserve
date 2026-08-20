package reserve.support;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import java.io.OutputStream;
import java.io.PrintStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

@Slf4j
@HttpIntegrationTest
public abstract class BaseRestAssuredTest {

    @Autowired
    private TestStateCleaner testStateCleaner;

    protected RequestSpecification spec;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUpSpec() {
        spec = new RequestSpecBuilder().setBaseUri("http://localhost")
            .setPort(port)
            .addFilter(RequestLoggingFilter.logRequestTo(createRedirectedPrintStream("Request:\n")))
            .addFilter(ResponseLoggingFilter.logResponseTo(createRedirectedPrintStream("Response:\n")))
            .build();
    }

    @AfterEach
    void cleanState() {
        testStateCleaner.cleanUp();
    }

    private PrintStream createRedirectedPrintStream(String prefix) {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }

            @Override
            public void write(byte[] b, int off, int len) {
                String msg = new String(b, off, len);
                log.debug("{}{}", prefix, msg);
            }
        });
    }

}
