package org.example;

import org.example.entities.Train;
import org.example.entities.User;
import org.example.services.UserBookingService;
import org.example.util.UserServiceUtil;

import java.io.IOException;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Ticket Booking System Running...");
        Scanner scanner = new Scanner(System.in);
        int option = 0;

        UserBookingService userBookingService;

        try{
            userBookingService = new UserBookingService();

        } catch (IOException e) {
            System.out.println("Something went wrong while initialisation!");
            throw new RuntimeException(e);
        }

        Train trainSelectedForBooking = new Train();
        Optional<User> foundUser = null; //LoggedIn User
        String source="", destination = "";//Travel Locations

        while(option != 7){
            System.out.println("Choose option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");
            option = scanner.nextInt();

            switch (option){
                case 1:
                    System.out.println("Enter the username to signup: ");
                    String nameToSignUp = scanner.next().trim();
                    System.out.println("Enter the password to signup: ");
                    String passwordToSignUp = scanner.next().trim();

                    User userToSignUp = new User(nameToSignUp,passwordToSignUp,
                            UserServiceUtil.hashPassword(passwordToSignUp),
                            new ArrayList<>(), UUID.randomUUID().toString());

                    userBookingService.signUp(userToSignUp);
                    break;
                case 2:
                    System.out.println("Enter the username to login: ");
                    String nameToLogin = scanner.next().trim();
                    System.out.println("Enter the password to login: ");
                    String passwordToLogin = scanner.next().trim();

                    User userToLogin = new User(nameToLogin,passwordToLogin,
                            UserServiceUtil.hashPassword(passwordToLogin),
                            new ArrayList<>(),"");

                    foundUser = userBookingService.loginUser(userToLogin);

                    if(foundUser.isPresent()){
                        userBookingService = new UserBookingService(foundUser.get());
                        System.out.println("Login Successful!");
                    }else{
                        System.out.println("Unable to login!");
                    }
                    break;
                case 3:
                    if(foundUser != null && foundUser.isPresent()){
                        System.out.println("Fetching your bookings...");
                        userBookingService.fetchBooking();
                    }else{
                        System.out.println("Please login first!");
                    }
                    break;
                case 4:
                    if(foundUser != null && foundUser.isPresent()){
                        System.out.println("Enter source station: ");
                        source = scanner.next().trim();
                        System.out.println("Enter destination station: ");
                        destination = scanner.next().trim();

                        List<Train> trains = userBookingService.getTrains(source,destination);
                        int trainCount = 0;
                        for(Train train : trains){
                            System.out.println(trainCount);
                            System.out.println("Train ID: "+train.getTrainId());
                            Map<String,String> stationTimes = train.getStationTimes();

                            for(String station : stationTimes.keySet()){
                                System.out.println("Station: "+station+" Time: "+stationTimes.get(station));
                            }
                            System.out.println();
                            trainCount++;
                        }

                        System.out.println("Select a train by typing 0,1,2,3..");
                        trainSelectedForBooking = trains.get(scanner.nextInt());
                    }else{
                        System.out.println("Please login first!");
                    }
                    break;
                case 5:
                    if(foundUser != null && foundUser.isPresent()){
                        if(trainSelectedForBooking!=null){
                            System.out.println("Select a seat out of these seats: ");
                            List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);
                            //Printing seats
                            for(List<Integer> row : seats){
                                for(int seat : row){
                                    System.out.print(seat +" ");
                                }
                                System.out.println();
                            }

                            System.out.println("Select the seat by typing the row and column");
                            System.out.println("Enter the row");
                            int row = scanner.nextInt();
                            System.out.println("Enter the column");
                            int col = scanner.nextInt();
                            System.out.println("Enter date of travel (DD/MM/YYYY): ");
                            String dateOfTravel = scanner.next().trim();
                            System.out.println("Booking your seat....");

                            boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking,row,col,source,destination,dateOfTravel);

                            if(booked){
                                System.out.println("Ticket Booked!");
                            }else{
                                System.out.println("Can't book this ticket!");
                            }
                        }else{
                            System.out.println("Please select a train first!");
                        }
                    }else{
                        System.out.println("Please login first!");
                    }
                    break;
                case 6:
                    if(foundUser != null && foundUser.isPresent()){
                        System.out.println("Enter ticket id to cancel: ");
                        String ticketToCancel = scanner.next().trim();
                        boolean ticketCancelled = userBookingService.cancelBooking(ticketToCancel);
                        if(ticketCancelled){
                            System.out.println("Ticket Id: "+ticketToCancel+"\nCancelled!");
                        }else{
                            System.out.println("Ticket not cancelled!");
                        }
                    }else{
                        System.out.println("Please login first!");
                    }
                    break;
                case 7:
                    System.out.println("Thank you for using our system!");
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Please enter a valid input!");
                    break;
            }
        }
    }
}