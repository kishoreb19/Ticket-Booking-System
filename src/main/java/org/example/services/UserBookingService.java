package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Ticket;
import org.example.entities.Train;
import org.example.entities.User;
import org.example.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserBookingService {
    private User user;
    private List<User> userList;

    private static final String USER_PATH = "src/main/java/org/example/localDb/users.json";
    private ObjectMapper objectMapper = new ObjectMapper();

    public UserBookingService(User user) throws IOException {
        this.user = user;
        userList = loadUsers();
    }

    public UserBookingService() throws IOException {
        userList = loadUsers();
    }

    public List<User> loadUsers() throws IOException {
        //Read JSON file
        File users = new File(USER_PATH);
        //Deserialization (JSON -> Object)
        return objectMapper.readValue(users, new TypeReference<List<User>>() {
        });
    }
    public Optional<User> loginUser(User userToLogin){
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equalsIgnoreCase(userToLogin.getName()) && UserServiceUtil.checkPassword(userToLogin.getPassword(),user1.getHashedPassword());
        }).findFirst();

        return foundUser;
    }

    public boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return true;
        }catch (IOException ioException){
            return false;
        }
    }

    private void saveUserListToFile() throws IOException {
        File users = new File(USER_PATH);
        objectMapper.writeValue(users,userList);
    }

    public void fetchBooking(){
        user.printTickets();
    }

    public boolean cancelBooking(String ticketId) throws IOException {
        //TODO
        List<Ticket> tempTickets = user.getTicketsBooked();
        Optional<Ticket> ticketToDel = tempTickets.stream().filter(ticket -> ticket.getTicketId().equalsIgnoreCase(ticketId)).findFirst();

        if(ticketToDel.isPresent()){
            boolean deleted = tempTickets.remove(ticketToDel.get());
            if(deleted){
                Optional<User> currUser = userList.stream().filter(user1 -> user1.getName().equalsIgnoreCase(user.getName())).findFirst();
                if(currUser.isPresent()){
                    currUser.get().setTicketsBooked(tempTickets);
                    saveUserListToFile();
                    return true;
                }else{
                    System.out.println("Unable to process ticket!");
                    return false;//Ticket not deleted
                }
            }else {
                return false;//Ticket not deleted
            }
        }else {
            return false;//Ticket not present
        }
    }

    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        }catch(IOException ex){
            System.out.println("Something went wrong while fetching trains!");
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public boolean bookTrainSeat(Train train, int row, int seat, String source, String destination, String dateOfTravel) throws IOException {
        List<List<Integer>> seats = train.getSeats();
        TrainService trainService = new TrainService();
        if(0<=row && row<seats.size() && 0<=seat && seat<seats.get(row).size()){

            if(seats.get(row).get(seat) == 0) {//If not booked

                Optional<User> currUser = userList.stream().filter(user1 -> user1.getName().equalsIgnoreCase(user.getName())).findFirst();
                if(currUser.isPresent()){

                    seats.get(row).set(seat,1);

                    //Creating Ticket
                    Ticket ticket = new Ticket(UUID.randomUUID().toString(),
                            user.getUserId(),source,destination,dateOfTravel,row,seat,train);

                    user.getTicketsBooked().add(ticket);

                    currUser.get().setTicketsBooked(user.getTicketsBooked());
                    saveUserListToFile();
                }else{
                    System.out.println("Unable to process ticket!");
                }

                trainService.addTrain(train);
                return true;
            }else{
                return false;//Seat not available
            }
        }else{
            return false;//Invalid row or seat index
        }
    }
}