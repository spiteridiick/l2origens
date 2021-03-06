package org.l2j.commons.database.helpers;

import io.github.joealisson.primitive.maps.IntObjectMap;
import io.github.joealisson.primitive.pair.IntObjectPair;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

public class QueryDescriptor implements AutoCloseable {

    private static final Pattern SELECT_PATTERN = Pattern.compile("^SELECT.*", Pattern.CASE_INSENSITIVE);
    private static final NoParameterStrategy NO_PARAMETER_STRATEGY = new NoParameterStrategy();
    private static final ThreadLocal<Statement> statementLocal = new ThreadLocal<>();

    private final String query;
    private final Method method;
    private final MapParameterStrategy strategy;

    public QueryDescriptor(Method method, String query) {
        this(method, query, NO_PARAMETER_STRATEGY);
    }

    public QueryDescriptor(Method method, String query, IntObjectMap<IntObjectPair<Class<?>>> parametersInfo) {
        this(method, query, new IndexedValuesStrategy(parametersInfo));
    }

    public QueryDescriptor(Method method, String query, MapParameterStrategy strategy) {
        this.query = query;
        this.method = method;
        this.strategy = strategy;
    }

    public boolean isUpdate() {
        return !SELECT_PATTERN.matcher(query).matches();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Type getGenericReturnType() {
        return method.getGenericReturnType();
    }

    public ResultSet getResultSet() throws SQLException {
        var statement = statementLocal.get();
        return nonNull(statement) ? statement.getResultSet() : null;
    }

    @Override
    public void close() throws SQLException {
        var statement = statementLocal.get();
        if(nonNull(statement)) {
            statement.close();
            statementLocal.remove();
        }
    }

    public void execute(Connection con, Object[] args) throws SQLException {
        var statement = con.prepareStatement(query);
        strategy.setParameters(statement, args);
        statement.execute();
        statementLocal.set(statement);
    }

    public Integer getUpdateCount() throws SQLException {
        var statement = statementLocal.get();
        return nonNull(statement) ? statement.getUpdateCount() : 0;
    }
}
