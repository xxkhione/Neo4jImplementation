/**
 * @author dsargent
 * @createdOn 8/27/2024 at 12:48 PM
 * @projectName Neo4jImplementation
 * @packageName edu.neumont.dbt230.controller;
 */
package edu.neumont.dbt230.controller;


import edu.neumont.dbt230.model.Employee;

import edu.neumont.dbt230.view.Display;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Neo4JInteraction {
    public static final Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo12345"));
    public static final File FILE_PATH = new File("C:/Courses/Q4/DBT230/PeopleData/people/long");
    public static final File FRIENDSHIPS_PATH = new File("C:/Courses/Q4/DBT230/Neo4JRelationships/friendships-1.csv");
    public static final File REPORTSTO_PATH = new File("C:/Courses/Q4/DBT230/Neo4JRelationships/reportsTo-1.csv");
    public static int maxID;

    public static List<String> getFiles(){
        if(FILE_PATH.exists()){
            String[] employeeFiles = FILE_PATH.list();
            List<String> fileContent = new ArrayList<>();
            for(String employeeFile : employeeFiles){
                fileContent.add(readFile(employeeFile));
            }
            return fileContent;
        }
        return null;
    }
    public static List<Employee> getEmployeeData(List<String> employeeInfo){
        List<Employee> employees = new ArrayList<>();
        for(String info : employeeInfo){
            employees.add(getSingleEmployee(info));
        }
        return employees;
    }
    public static Employee getSingleEmployee(String info){
        Employee employee = null;
        int id = 0;
        String firstName = null;
        String lastName = null;
        int hireYear = 0;

        String[] infoParts = info.split(", ");
        id = Integer.parseInt(infoParts[0]);
        firstName = infoParts[1];
        lastName = infoParts[2];
        hireYear = Integer.parseInt(infoParts[3]);
        return employee = new Employee(id, firstName, lastName, hireYear);
    }

    public static void insertBulkEmployees(){
        for(Employee employee : getEmployeeData(getFiles())){
            insertOneEmployee(employee);
        }
    }
    public static void insertFriendshipRelationships(){
        try (Session session = driver.session()){
            BufferedReader br = new BufferedReader(new FileReader(FRIENDSHIPS_PATH));
            br.readLine(); // Skips the headers
            String line = "";
            while((line = br.readLine()) != null){
                String[] ids = line.split(",");
                int idOne = Integer.parseInt(ids[0]); //pid
                int idTwo = Integer.parseInt(ids[1]); //friendid

                String cypherQuery = "MATCH (e1:Employee {id: $pid}), (e2:Employee {id: $friendid}) CREATE (e1)-[:FRIENDSHIP]->(e2)";
                session.run(cypherQuery, Values.parameters("pid", idOne, "friendid", idTwo));
            }
            br.close();
        } catch (IOException ioe){}
    }
    public static void insertReportsToRelationships(){
        try (Session session = driver.session()){
            BufferedReader br = new BufferedReader(new FileReader(REPORTSTO_PATH));
            br.readLine(); // Skips the headers
            String line = "";
            while((line = br.readLine()) != null){
                String[] ids = line.split(",");
                int idOne = Integer.parseInt(ids[0]); //bossid
                int idTwo = Integer.parseInt(ids[1]); //pid

                String cypherQuery = "MATCH (e1:Employee {id: $pid}), (e2:Employee {id: $bossid}) CREATE (e1)-[:REPORTS_TO]->(e2)";
                session.run(cypherQuery, Values.parameters("pid", idOne, "bossid", idTwo));
            }
            br.close();
        } catch (IOException ioe){}
    }

    public static void insertOneEmployee(Employee employee) {
        String cypherQuery = "CREATE (e:Employee {id: $id, firstName: $firstName, lastName: $lastName, hireYear: $hireYear}) RETURN e";

        try (Session session = driver.session()) {
            Result result = session.run(cypherQuery, Map.of("id", employee.getId(), "firstName", employee.getFirstName(), "lastName", employee.getLastName(), "hireYear", employee.getHireYear()));
            if(result.hasNext()){
                String relationshipQuery = "MATCH (e1:Employee {id: $pid}), (e2:Employee {id: $bossid}) CREATE (e1)-[:REPORTS_TO]->(e2)";
                session.run(relationshipQuery, Values.parameters("bossid", 10000, "pid", employee.getId()));
                Display.successfulMsg();
            } else {
                Display.errorMsg();
            }
        }
    }
    public static void deleteEmployee(int id){
        try(Session session = driver.session()){
            String cypherQuery = "MATCH (e:Employee {id: $id}) DETACH DELETE e";
            session.run(cypherQuery, Values.parameters("id", id));
        }
    }
    public static void updateAnEmployee(int id, int option, String updatedValue){
        try (Session session = driver.session()){
            String cypherQuery = "MATCH (e:Employee) WHERE e.id = $id SET ";
            switch(option){
                case 1: //update firstName
                    cypherQuery += "e.firstName = $firstName";
                    session.run(cypherQuery, Values.parameters("id", id, "firstName", updatedValue));
                    break;
                case 2: //update lastName
                    cypherQuery += "e.lastName = $lastName";
                    session.run(cypherQuery, Values.parameters("id", id, "lastName", updatedValue));
                    break;
                case 3: //update hireYear
                    cypherQuery += "e.hireYear = $hireYear";
                    session.run(cypherQuery, Values.parameters("id", id, "hireYear", Integer.parseInt(updatedValue)));
                    break;
            }
        }
    }
    public static List<Employee> getAllEmployees(){
        try (Session session = driver.session()){
            Result result = session.run("MATCH (e:Employee) RETURN e");
            List<Employee> employees = new ArrayList<>();

            while(result.hasNext()){
                Record record = result.next();
                Node employeeNode = record.get("e").asNode();
                int id = employeeNode.get("id").asInt();
                String firstName = employeeNode.get("firstName").asString();
                String lastName = employeeNode.get("lastName").asString();
                int hireYear = employeeNode.get("hireYear").asInt();

                Employee employee = new Employee(id, firstName, lastName, hireYear);
                employees.add(employee);
            }
            return employees;
        }
    }
    public static Employee getOneEmployee(int id){
        try (Session session = driver.session()){
            Result result = session.run("MATCH (e:Employee {id: $id}) RETURN e", Values.parameters("id", id));
            if(result.hasNext()){
                Record record = result.single();
                Node employeeNode = record.get("e").asNode();
                String firstName = employeeNode.get("firstName").asString();
                String lastName = employeeNode.get("lastName").asString();
                int hireYear = employeeNode.get("hireYear").asInt();

                Employee searchedEmployee = new Employee(id, firstName, lastName, hireYear);
                return searchedEmployee;
            }
        }
        return null;
    }

    public static void getMaxID(){
        try (Session session = driver.session()){
            Result result = session.run("MATCH (e:Employee) RETURN max(e.id) AS maxID");
            if(result.hasNext()){
                maxID = result.next().get("maxID").asInt();
            }
        }
    }

    private static String readFile(String id) {
        String fileInformation = "";
        try{
            BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_PATH + "/" + id)));
            while(bReader.ready()){
                fileInformation = bReader.readLine();
            }
        } catch(IOException ioe){}
        return fileInformation;
    }
}