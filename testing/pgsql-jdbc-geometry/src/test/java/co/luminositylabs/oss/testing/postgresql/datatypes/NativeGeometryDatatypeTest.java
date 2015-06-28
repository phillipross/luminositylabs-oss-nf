package co.luminositylabs.oss.testing.postgresql.datatypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.UUID;


/**
 * This class contains test code for exercising persistence of PostgreSQL native geometry datatypes
 * via the postgresql (org.postgresql) jdbc driver.
 *
 * @author Phillip Ross {@literal <phillip.w.g.ross@gmail.com>}
 */
public class NativeGeometryDatatypeTest {

    private static final Logger logger = LoggerFactory.getLogger(NativeGeometryDatatypeTest.class);

    private static final String JDBC_URL_DRIVER = "postgresql";

    private static final String JDBC_URL_DATABASE_HOSTNAME_AND_PORT = "localhost:5432";

    private static final String JDBC_URL_DATABASE_NAME = "test_db";

    private static final String JDBC_DATABASE_USERNAME = "test_db_username";

    private static final String JDBC_DATABASE_PASSWORD = "test_db_password";


    private static final String[] testData = new String[] {
            "point    => (1,1)"
            ,"lseg    => [(5,6),(7,8)]"
            ,"box     => (9,10),(11,12)"
            ,"path    => [(1,1),(2,2),(3,3)]"
            ,"polygon => ((4,4),(5,5),(6,6),(7,7),(8,8))"
            ,"circle  => <(9,9),1>"
//            ,"line    => {1,2,3}"
    };

    private Connection jdbcConnection = null;


    @BeforeClass
    public void setupJdbcResources()  throws ClassNotFoundException, SQLException {
        logger.debug("Loading jdbc driver class");
        Class.forName("org.postgresql.Driver");
        logger.debug("Obtaining jdbc connection");
        jdbcConnection = DriverManager.getConnection("jdbc:" + JDBC_URL_DRIVER + "://" + JDBC_URL_DATABASE_HOSTNAME_AND_PORT + "/" + JDBC_URL_DATABASE_NAME, JDBC_DATABASE_USERNAME, JDBC_DATABASE_PASSWORD);
    }


    @AfterClass
    public void disposeJdbcResources() throws SQLException {
        if ((jdbcConnection != null) && (!(jdbcConnection.isClosed()))) {
            jdbcConnection.close();
        }
    }


    @Test
    public void testGeometryDatatypes() throws Exception {
        logger.trace("void testGeometryDatatypes()");

        String tableName = generateTestTableName();
        createTestTable(tableName);

        try {
            for (int i = 0; i < testData.length; i++) {
                logger.debug("testing with test data: {}", testData[i]);
                testDatatype(tableName, i);
            }
        } finally {
            dropTestTable(tableName);
        }
    }


    private void testDatatype(String tableName, int testDataIndex) throws SQLException {
        String testDataString = testData[testDataIndex];
        String[] testDataStringElements = testDataString.split("=>");
        String dataTypeName = testDataStringElements[0].trim();
        String testDataLiteral = testDataStringElements[1].trim();

        logger.debug("inserting test record for {} data", dataTypeName.toUpperCase());
        UUID recordId = UUID.randomUUID();
        String insertStatement1 = "insert into " + tableName + " (_id, example_" + dataTypeName + ") values ( ?,  '" + testDataLiteral + "')";
        PreparedStatement preparedStatement = jdbcConnection.prepareStatement(insertStatement1);
        preparedStatement.setObject(1, recordId);
        preparedStatement.execute();
        preparedStatement.close();

        logger.debug("querying test record for {} data", dataTypeName.toUpperCase());
        String selectStatement1 = "select example_" + dataTypeName + " from " + tableName + " where _id = ?";
        preparedStatement = jdbcConnection.prepareStatement(selectStatement1);
        preparedStatement.setObject(1, recordId);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Object resultObject = resultSet.getObject(1);
            logger.debug("result (class / value) --> ({} / {})", resultObject.getClass().getName(), resultObject);
        }
        resultSet.close();
        preparedStatement.close();
    }


    private String generateTestTableName() throws SQLException {
        logger.debug("Generating new test table");
        String tableName = null;
        for (int i = 0; i < 10; i++) {
            tableName = "test_table_" + UUID.randomUUID().toString().replaceAll("-", "");
            DatabaseMetaData databaseMetaData = jdbcConnection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, tableName.toLowerCase(), new String[] {"TABLE"})) {
                if (resultSet.next()) {
                    tableName = null;
                } else {
                    break;
                }
            }
        }
        Assert.assertNotNull(tableName, "Unable to generate a unique table.");
        logger.debug("name of new test table is {}", tableName);
        return tableName;
    }


    private void createTestTable(String tableName) throws SQLException {

        logger.debug("Creating new test table");
        StringBuilder tableCreateStatementBuilder = new StringBuilder("create table ")
                .append(tableName)
                .append(" ( ")
                .append("_ID uuid").append(", ");
        for (String dataTypeNameElement : testData) {
            String dataTypeName = dataTypeNameElement.split("=>")[0].trim();
            tableCreateStatementBuilder.append("example_")
                    .append(dataTypeName)
                    .append(" ")
                    .append(dataTypeName)
                    .append(", ");
        }
        tableCreateStatementBuilder.delete(
                tableCreateStatementBuilder.lastIndexOf(","),
                tableCreateStatementBuilder.length()
        );
        tableCreateStatementBuilder.append(" ) ");
        String tableCreationStatement = tableCreateStatementBuilder.toString();
        logger.debug("table create statement: {}", tableCreationStatement);

        try (CallableStatement callableStatement = jdbcConnection.prepareCall(tableCreationStatement)) {
            callableStatement.execute();
        }
    }


    private void dropTestTable(String tableName) throws SQLException {
        logger.debug("Dropping test table");
        String tableDropStatement = "drop table " + tableName + ";";
        logger.debug("table drop statement => [{}]", tableDropStatement);
        try (CallableStatement callableStatement = jdbcConnection.prepareCall(tableDropStatement)) {
            callableStatement.execute();
        }
    }


}