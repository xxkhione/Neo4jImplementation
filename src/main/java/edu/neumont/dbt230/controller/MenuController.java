/**
 * @author dsargent
 * @createdOn 8/27/2024 at 12:48 PM
 * @projectName Neo4jImplementation
 * @packageName edu.neumont.dbt230.controller;
 */
package edu.neumont.dbt230.controller;

import edu.neumont.dbt230.model.Employee;
import edu.neumont.dbt230.view.Display;


public class MenuController {
    public static void run() {
        Neo4JInteraction.getMaxID();
        long startTime;
        long endTime;
        do{
            startTime = System.nanoTime();
            Display.welcomeMsg();
            int selection = Display.mainMenu();
            switch(selection) {
                case 1: // 1. Create an employee file
                    String fName = Display.getFirstName();
                    String lName = Display.getLastName();
                    int hireYear = Display.getHireYear();
                    Employee newEmployee = new Employee(Neo4JInteraction.maxID + 1, fName, lName, hireYear);
                    Neo4JInteraction.insertOneEmployee(newEmployee);
                    Neo4JInteraction.maxID++;
                    break;
                case 2: // 2. Update an employee file
                    int id = Display.getIDSearch();
                    int option = Display.updateMenu();
                    switch(option) {
                        case 1: // First Name
                            String updatedFirstName = Display.updateFirstName();
                            Neo4JInteraction.updateAnEmployee(id, option, updatedFirstName);
                            break;
                        case 2: // Last Name
                            String updatedLastName = Display.updateLastName();
                            Neo4JInteraction.updateAnEmployee(id, option, updatedLastName);
                            break;
                        case 3: // Hire Year
                            int updatedHireYear = Display.updateHireYear();
                            Neo4JInteraction.updateAnEmployee(id, option, String.valueOf(updatedHireYear));
                            break;
                    }
                    break;
                case 3: // 3. Delete an employee file
                    int idDelete = Display.getIDSearch();
                    Neo4JInteraction.deleteEmployee(idDelete);
                    Display.successfulMsg();
                    break;
                case 4: // 4. Search for an employee
                    int searchID = Display.getIDSearch();
                    Employee searchedEmployee = Neo4JInteraction.getOneEmployee(searchID);
                    if(searchedEmployee != null){
                        Display.printSingleEmployee(searchedEmployee);
                    } else { Display.errorMsg(); }
                    break;
                case 5: // 5. View all employees
                    Display.printEmployees();
                    break;
                default: // 6. Quit
                    Display.quit();
                    Neo4JInteraction.driver.close();
                    return;
            }
            endTime = System.nanoTime();
            Display.printTimeTakenToExecute((endTime - startTime));
        } while(true);
    }
}
