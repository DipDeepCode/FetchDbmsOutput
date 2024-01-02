package ru.ddc.fetch_dbms_output;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.stream.Stream;

@SpringBootApplication
public class FetchDbmsOutput implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public FetchDbmsOutput(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... strings) throws Exception {
        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("begin dbms_output.enable(); end;");
        statement.executeUpdate("begin my_procedure(1, 2); end;");
        CallableStatement call = connection.prepareCall(
                "declare num integer := 1000; begin dbms_output.get_lines(?, num); end;");
        call.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
        call.execute();
        Array array = call.getArray(1);
        Stream.of((String[]) array.getArray()).forEach(System.out::println);
        array.free();
        statement.executeUpdate("begin dbms_output.disable(); end;");
        statement.close();
        connection.close();
    }

    public static void main(String[] args) {
        SpringApplication.run(FetchDbmsOutput.class, args);
    }
}
