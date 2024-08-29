package reserve.global;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(RestDocumentationExtension.class)
public abstract class BaseRestAssuredTest {

    protected static final String DEFAULT_RESTDOC_PATH = "{class_name}/{method_name}/";

    protected RequestSpecification spec;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @BeforeEach
    void setUpRestDocs(RestDocumentationContextProvider provider) {
        spec = new RequestSpecBuilder()
                .setPort(port)
                .setBaseUri("https://localhost")
                .addFilter(
                        documentationConfiguration(provider)
                                .operationPreprocessors()
                                .withRequestDefaults(prettyPrint())
                                .withResponseDefaults(prettyPrint())
                )
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
