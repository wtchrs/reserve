package reserve.room.infrastructure;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.BasicTypeReference;

import java.util.List;

import static org.hibernate.type.StandardBasicTypes.DOUBLE;

/**
 * A custom JPQL function for performing full-text search using the MATCH AGAINST syntax of MySQL.
 * This class extends the {@link StandardSQLFunction} and overrides the {@code render} method to generate
 * SQL statements for full-text search queries.
 *
 * <p> JPQL Usage Example: </p>
 * <pre> function_name(column1[, column2, ...], 'query_string') </pre>
 * This JPQL function call will result in a native query like:
 * <pre> MATCH(column1[, column2, ...]) AGAINST('query_string' IN BOOLEAN MODE) </pre>
 *
 * Note:
 * <ul>
 *     <li> Replace {@code function_name} in the example with the name passed to the constructor. </li>
 *     <li> The {@code column1}, {@code column2}, ... must not be enclosed in quotes because they represent column names,
 *       not string values. </li>
 * </ul>
 *
 * @author wtchrs
 */
public class FullTextSearchSQLFunction extends StandardSQLFunction {

    private static final BasicTypeReference<Double> RETURN_TYPE = DOUBLE;

    /**
     * Constructs a new FullTextSearchSQLFunction with the given name.
     *
     * @param name The name of the SQL function.
     */
    public FullTextSearchSQLFunction(String name) {
        super(name, true, RETURN_TYPE);
    }

    @Override
    public void render(
            SqlAppender sqlAppender,
            List<? extends SqlAstNode> sqlAstArguments,
            ReturnableType<?> returnType,
            SqlAstTranslator<?> translator
    ) {
        int argumentsSize = sqlAstArguments.size();
        sqlAppender.append("MATCH(");
        for (int i = 0; i < argumentsSize - 2; i++) {
            sqlAstArguments.get(i).accept(translator);
            sqlAppender.append(", ");
        }
        sqlAstArguments.get(argumentsSize - 2).accept(translator);
        sqlAppender.append(") AGAINST(");
        sqlAstArguments.get(argumentsSize - 1).accept(translator);
        sqlAppender.append(" IN BOOLEAN MODE)");
    }

}
