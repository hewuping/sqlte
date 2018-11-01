package hwp.sqlte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.sql.*;

class ScriptRunner {

    private Logger logger = LoggerFactory.getLogger("sql");
    private static final String DEFAULT_DELIMITER = ";";

    private Connection connection;

    private boolean stopOnError;
    private boolean autoCommit;

    private String delimiter = DEFAULT_DELIMITER;

    private boolean fullLineDelimiter = false;


    public ScriptRunner(boolean stopOnError, boolean autoCommit) {
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    public void runScript(Connection conn, InputStream in) throws UncheckedSQLException {
        runScript(conn, new InputStreamReader(in));
    }

    public void runScript(Connection conn, URL in) throws UncheckedSQLException {
        try (Reader reader = new InputStreamReader(in.openStream())) {
            runScript(conn, reader);
        } catch (IOException e) {
            throw new UncheckedSQLException(e);
        }
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the
     * connection passed in
     *
     * @param conn   - the connection to use for the script
     * @param reader - the source of the script
     * @throws SQLException if any SQL errors occur
     * @throws IOException  if there is an error reading from the Reader
     */
    public void runScript(Connection conn, Reader reader) throws UncheckedSQLException {
        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--") || trimmedLine.length() < 1) {
                    continue;
                }
                if (!fullLineDelimiter && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter && trimmedLine.equals(getDelimiter())) {
                    command.append(line, 0, line.lastIndexOf(getDelimiter()));
                    command.append(" ");
                    try (Statement statement = conn.createStatement()) {
                        boolean hasResults = false;
                        if (stopOnError) {
                            hasResults = statement.execute(command.toString());
                        } else {
                            try {
                                statement.execute(command.toString());
                            } catch (SQLException e) {
                                logger.error(e.getMessage());
                            }
                        }
                        if (autoCommit && !conn.getAutoCommit()) {
                            conn.commit();
                        }
                        try (ResultSet rs = statement.getResultSet()) {
                            if (hasResults && rs != null) {
                                StringBuilder builder = new StringBuilder();
                                ResultSetMetaData md = rs.getMetaData();
                                int cols = md.getColumnCount();
                                for (int i = 0; i < cols; i++) {
                                    String name = md.getColumnLabel(i);
                                    builder.append(name).append("\t");
                                }
                                builder.append('\n');
                                while (rs.next()) {
                                    for (int i = 0; i < cols; i++) {
                                        String value = rs.getString(i);
                                        builder.append(value).append("\t");
                                    }
                                    builder.append('\n');
                                }
                                logger.info("Result:\n{}", builder);
                            }
                        }
                        command = null;
                    }
                    Thread.yield();
                } else {
                    command.append(line);
                    command.append(" ");
                }
            }
            if (!autoCommit) {
                conn.commit();
            }
        } catch (SQLException | IOException e) {
            throw new UncheckedSQLException(e);
        } finally {
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private String getDelimiter() {
        return delimiter;
    }

}