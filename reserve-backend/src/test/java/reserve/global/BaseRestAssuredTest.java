package reserve.global;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.OutputStream;
import java.io.PrintStream;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseRestAssuredTest {

    protected RequestSpecification spec;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @BeforeEach
    void setUpSpec() {
        spec = new RequestSpecBuilder()
                .setPort(port)
                .setBaseUri("https://localhost")
                .build();
        RestAssured.filters(
                RequestLoggingFilter.logRequestTo(createRedirectedPrintStream("Request:\n")),
                ResponseLoggingFilter.logResponseTo(createRedirectedPrintStream("Response:\n"))
        );
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
