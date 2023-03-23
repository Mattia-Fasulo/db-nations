package org.exercises.nations;

import java.sql.*;
import java.util.Scanner;

public class Main {

    private final static String URL = System.getenv("DB_URL");
    private final static String USER = System.getenv("DB_USER");
    private final static String PASSWORD = System.getenv("DB_PASSWORD");


    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        //provo ad aprire una connessione con i parametri passati
        try(Connection connection = DriverManager.getConnection(URL,USER,PASSWORD)){

            String query = """
                    select c.name as country, r.name as region, c2.name as continent
                    from countries c\s
                    join regions r on r.region_id = c.region_id\s
                    join continents c2  on c2.continent_id = r.continent_id\s
                    where c.name like "%"?"%"
                    order by c.name;\s
                    """;

            System.out.print("search for a country: ");
            String searchString = scan.nextLine();

            //chiedo alla connection di preparare uno statement SQL con la query passata come parametro
            try(PreparedStatement ps = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)){

                ps.setString(1, searchString);
                //provo a eseguire la query e se va a buon fine metto il risultato nel ResultSet
                try(ResultSet rs = ps.executeQuery()){
                    //controllo che il ResultSet non sia vuoto
                    if(!rs.next()){
                        System.out.println("No countries found");
                    } else {
                        //se non è vuoto riporto il puntatore del ResultSet all'inizio
                        rs.beforeFirst();
                    }

                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");
                    printResultSetLine("country                   ","region                   ", "continent                   ");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");
                    while(rs.next()){

                        String country = rs.getString(1);
                        String region = rs.getString(2);
                        String continent = rs.getString(3);



                        printResultSetLine(country, region, continent);
                    }
                }   System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void printResultSetLine(String country, String region, String continent) {
        String stringPattern = "%45s";

        System.out.printf(stringPattern, country);
        System.out.printf(stringPattern, region);
        System.out.printf(stringPattern + "\n", continent);
    }
}
