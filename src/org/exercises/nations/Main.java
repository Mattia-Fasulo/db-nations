package org.exercises.nations;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
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
                    select c.country_id as id, c.name as country, r.name as region, c2.name as continent
                    from countries c\s
                    join regions r on r.region_id = c.region_id\s
                    join continents c2  on c2.continent_id = r.continent_id\s
                    where c.name like "%"?"%"
                    order by c.name;\s
                    """;

            System.out.println();
            System.out.print("search for a country: ");
            String searchString = scan.nextLine();
            System.out.println();

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

                    System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");
                    printTableTitles();
                    System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");
                    while(rs.next()){

                        int id = rs.getInt(1);
                        String country = rs.getString(2);
                        String region = rs.getString(3);
                        String continent = rs.getString(4);



                        printResultSetLine(id, country, region, continent);
                    }
                }   System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println();

        try(Connection connection = DriverManager.getConnection(URL,USER,PASSWORD)){

            System.out.print("chose a country id: ");
            int countryId = Integer.parseInt(scan.nextLine());


            String query = """
                    select c.name, l.`language`, cs.`year` , cs.population , cs.gdp\s
                    from countries c                 \s
                    join country_languages cl on cl.country_id =c.country_id\s
                    join languages l on l.language_id = cl.language_id\s
                    join country_stats cs on cs.country_id =c.country_id\s
                    where c.country_id = ?\s
                    and cs.`year`  =  (select max(`year`) from country_stats cs2 where cs2.country_id = c.country_id)
                    """;

            System.out.println();

            try(PreparedStatement ps = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)){

                ps.setInt(1, countryId);

                try(ResultSet rs = ps.executeQuery()){
                    if(!rs.next()){
                        System.out.println("No countries found");
                    } else {
                        //se non è vuoto riporto il puntatore del ResultSet all'inizio
                        rs.beforeFirst();
                    }

                    Map<String, String> statCountry = new HashMap<>();
                    Map<Integer, String> languagesCountry = new HashMap<>();
                    Integer count = 1;

                    while(rs.next()){

                        String name = rs.getString(1) ;
                        String language = rs.getString(2);
                        String year = String.valueOf(rs.getInt(3));
                        String population = String.valueOf(rs.getInt(4)) ;
                        String gdp = String.valueOf(rs.getBigDecimal(5));

                        statCountry.put("country", name);
                        statCountry.put("population", population);
                        statCountry.put("GDP", gdp);
                        statCountry.put("year", year);

                        languagesCountry.put(count,language);
                        count++;

                    }

                    System.out.println("Detail for country: " + statCountry.get("country"));

                    for (Map.Entry<String,String> entry : statCountry.entrySet()) {
                        if (entry.getKey().equals("country")){
                            continue;
                        }
                        System.out.println(entry.getKey() + " : " + entry.getValue());
                    }

                    System.out.print("languages: ");
                    for (Map.Entry<Integer,String> entry : languagesCountry.entrySet()) {
                        System.out.print(entry.getValue() +", ");
                    }

                    System.out.println();

                }
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        scan.close();

    }

    private static void printResultSetLine(int id, String country, String region, String continent) {
        String stringPattern = "%45s";
        String idPattern = "%4s";

        System.out.printf(idPattern, id);
        System.out.printf(stringPattern, country);
        System.out.printf(stringPattern, region);
        System.out.printf(stringPattern + "\n", continent);
    }

    private static void printTableTitles() {
        String stringPattern = "%45s";
        String idPattern = "%4s";

        System.out.printf(idPattern, "ID");
        System.out.printf(stringPattern, "COUNTRY");
        System.out.printf(stringPattern, "REGION");
        System.out.printf(stringPattern + "\n", "CONTINENT");
    }
}
