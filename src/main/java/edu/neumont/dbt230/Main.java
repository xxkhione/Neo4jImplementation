/**
 * @author dsargent
 * @createdOn 8/27/2024 at 12:47 PM
 * @projectName Neo4jImplementation
 * @packageName edu.neumont.dbt230;
 */
package edu.neumont.dbt230;

import edu.neumont.dbt230.controller.MenuController;
import edu.neumont.dbt230.controller.Neo4JInteraction;

public class Main {
    public static void main(String[] args) {
        //Neo4JInteraction.insertBulkEmployees();
        //Neo4JInteraction.insertFriendshipRelationships();
        //Neo4JInteraction.insertReportsToRelationships();
        MenuController.run();
    }
}
