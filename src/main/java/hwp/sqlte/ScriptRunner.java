package hwp.sqlte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.sql.*;

class ScriptRunner {

    private final Logger logger = LoggerFactory.getLogger(ScriptRunner.class);
    private static final String DEFAULT_DELIMITER = ";";

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

    public void runScript(Connection conn, InputStream in) throws SqlteException {
        runScript(conn, new InputStreamReader(in));
    }

    public void runScript(Connection conn, URL in) throws SqlteException {
        try (Reader reader = new InputStreamReader(in.openStream())) {
            runScript(conn, reader);
        } catch (IOException e) {
            throw new SqlteException(e);
        }
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the
     * connection passed in
     *
     * @param conn   - the connection to use for the script
     * @param reader - the source of the script
     * @throws SqlteException if any SQL errors occur
     */
    public void runScript(Connection conn, Reader reader) throws SqlteException {
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
            throw new SqlteException(e);
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