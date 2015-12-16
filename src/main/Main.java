/*
 * Main class
 */
package main;

import java.util.Scanner;
import util.PropertiesUtil;

/**
 *
 * @author Gaurab Pradhan
 */
public class Main {

    public static void main(String[] args) {
        PropertiesUtil.loadPropertiesFile();
        Scanner reader = new Scanner(System.in);
        boolean flag = true;
        do {
            System.out.println("1. Update Winterisation Postgresql DB and Generate Dataset for winterisation Map");
            System.out.println("2. Generate Population of Concern Dataset");
            System.out.println("3. Exit");
            System.out.print("Enter your Choice(1 or 2) : ");
            int choice = reader.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Processing Winterisation PSQL DB");
                    winterisation_db.main(args);
                    break;
                case 2:
                    System.out.println("Preparing to Generate Population of Concern Dataset");
                    WinterUncover.main(args);
                    break;
                case 3:
                    System.exit(0);
                    break;
                default:
                    System.out.println("You did not enter a valid choice.");
                    break;
            }
            System.out.print("Do you want to continue (y/n) :");
            String ch = reader.next();
            if (ch.toLowerCase().equals("y")) {
                flag = true;
            } else {
                flag = false;
            }
        } while (flag);
    }
}
