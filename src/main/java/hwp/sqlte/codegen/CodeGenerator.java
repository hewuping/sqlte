package hwp.sqlte.codegen;


import hwp.sqlte.util.NameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Example:
 * <pre>{@code
 * generator.setJdbcUrl("jdbc:mysql://localhost:3306/demo");
 * generator.setUsername("root");
 * generator.setPassword("");
 * generator.setOutput("/your/path");
 * generator.generate();;
 * } </pre>
 */
public class CodeGenerator {
    private String packageName;
    private String output = "./";
    private String jdbcUrl;
    private String username;
    private String password;

/*    private DataSource dataSource;

    public CodeGenerator(DataSource dataSource) {
        this.dataSource = dataSource;
    }*/

    private DatabaseMetaData metaData;

    public void setOutput(String output) {
        this.output = output;
        if (!output.endsWith(File.separator)) {
            this.output += File.separator;
        }
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void generate() {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
//        try (Connection conn = dataSource.getConnection()) {
            this.metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("Generating entity for table " + tableName);
                generateClass(conn, tableName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void generateClass(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData dbmd = conn.getMetaData();
        ResultSet columns = dbmd.getColumns(null, null, tableName, "%");


        List<String> properties = generateProperties(tableName, columns);

        String className = NameUtils.toClassName(tableName);
        String filePath = output + className + ".java";

        try (FileWriter writer = new FileWriter(filePath)) {
            if (packageName != null && !packageName.isEmpty()) {
                writer.write("package " + packageName + ";\n\n");
            }
            writer.write("import hwp.sqlte.*;\n\n");
            writer.write("@Table(name = \"" + tableName + "\")\n");
            writer.write("public class " + className + " {\n\n");
            for (String property : properties) {
                writer.write(property);
            }
            writer.write("}\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> generateProperties(String tableName, ResultSet columns) throws SQLException {
        // 获取主键信息
        ResultSet keysResultSet = metaData.getPrimaryKeys(null, null, tableName);
        List<String> keys = new ArrayList<>();
        while (keysResultSet.next()) {
            String columnName = keysResultSet.getString("COLUMN_NAME");
            short keySeq = keysResultSet.getShort("KEY_SEQ");
            String pkName = keysResultSet.getString("PK_NAME");
            keys.add(columnName);


//            System.out.println("Column Name: " + columnName);
//            System.out.println("Key Sequence: " + keySeq);
//            System.out.println("PK Name: " + pkName);
//            System.out.println();
        }

        List<String> properties = new ArrayList<>();
        while (columns.next()) {
            String property = generateProperty(columns, keys);
            properties.add(property);
        }
        return properties;
    }


    private static String getJavaType(String columnType, int dataType) {
        String upperCaseColumnType = columnType.toUpperCase();
        // com.mysql.cj.MysqlType
        // org.postgresql.core.Oid
        //org.h2.value.Value
        switch (upperCaseColumnType) {
            case "VARCHAR":
            case "CHAR":
            case "TEXT":
            case "JSON":
                return "String";
            case "INT":
            case "INTEGER":
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
                return "Integer";
            case "BIGINT":
                return "Long";
            case "FLOAT":
                return "Float";
            case "DOUBLE":
                return "Double";
            case "DECIMAL":
                return "java.math.BigDecimal";
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
                return "java.util.Date";
            case "BOOLEAN":
                return "Boolean";
            default:
                return getJavaType(JDBCType.valueOf(dataType));
        }
    }


    private static String getJavaType(JDBCType jdbcType) {
        switch (jdbcType) {
            case VARCHAR:
            case CHAR:
            case NCHAR:
            case LONGVARCHAR:
            case NVARCHAR:
            case LONGNVARCHAR:
            case CLOB:
            case NCLOB:
                return "String";
            case BIT:
            case BOOLEAN:
                return "Boolean";
            case TINYINT:
                return "Byte";
            case SMALLINT:
                return "Short";
            case INTEGER:
                return "Integer";
            case BIGINT:
                return "Long";
            case REAL:
                return "Float";
            case FLOAT:
            case DOUBLE:
                return "Double";
            case NUMERIC:
            case DECIMAL:
                return "java.math.BigDecimal";
            case DATE:
            case TIME:
            case TIMESTAMP:
                return "java.sql.Timestamp";
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
            case BLOB:
                return "byte[]";
            default:
                return "Object";
        }
    }

    private String generateProperty(ResultSet column, List<String> keys) throws SQLException {
        String columnName = column.getString("COLUMN_NAME");
        int dataType = column.getInt("DATA_TYPE");
        String typeName = column.getString("TYPE_NAME");
        int size = column.getInt("COLUMN_SIZE");
        boolean isNullable = column.getBoolean("IS_NULLABLE");
        boolean isAutoincrement = false;
        try {
            isAutoincrement = column.getBoolean("IS_AUTOINCREMENT");
        } catch (SQLException e) {

        }

        StringBuilder sb = new StringBuilder();
        if (keys.contains(columnName)) {
            if (isAutoincrement) {
                sb.append("    @Id(generate=true)\n");
            } else {
                sb.append("    @Id\n");
            }
        }
        sb.append("    @Column(name = \"").append(columnName).append("\")\n");
        sb.append("    public ").append(getJavaType(typeName, dataType)).append(" ").append(NameUtils.toUpperCamel(columnName)).append(";\n\n");
        return sb.toString();
    }

    private boolean isAutoIncrement(String tableName, String columnName) throws SQLException {
        // 判断主键列是否为自动递增
        boolean isAutoIncrement = false;

        // pgsql
        ResultSet columnResultSet = metaData.getColumns(null, null, tableName, columnName);
        if (columnResultSet.next()) {
            String columnDefault = columnResultSet.getString("COLUMN_DEFAULT");
            if (columnDefault != null && columnDefault.startsWith("nextval")) {
                isAutoIncrement = true;
            }
        }
        return isAutoIncrement;
    }


}