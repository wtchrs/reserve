package reserve.store.infrastructure;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class FullTextSearchFunctionContributor implements FunctionContributor {

    private static final String FUNCTION_NAME = "fulltext_search";

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry().register(
                FUNCTION_NAME,
                new FullTextSearchSQLFunction(FUNCTION_NAME)
        );
    }

}
