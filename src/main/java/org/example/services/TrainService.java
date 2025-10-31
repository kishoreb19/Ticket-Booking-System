package org.example.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrainService {

    List<Train> trainList;
    ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAIN_DB_PATH = "src/main/java/org/example/localDb/trains.json";

    public TrainService() throws IOException {
        File trains = new File(TRAIN_DB_PATH);
        trainList = objectMapper.readValue(trains, new TypeReference<List<Train>>() {
        });
    }

    public List<Train> searchTrains(String source, String destination){
        return trainList.stream().filter(train -> validTrain(train,source,destination)).collect(Collectors.toList());
    }

    private boolean validTrain(Train train, String source, String destination){
        List<String> stations = train.getStations();

        int srcIndex = stations.indexOf(source);
        int destIndex = stations.indexOf(destination);

        return (srcIndex!=-1 && destIndex!=-1) && (srcIndex < destIndex);
    }

    public void addTrain(Train newTrain){
        Optional<Train> existingTrain = trainList.stream().
                filter(train -> train.getTrainId().equalsIgnoreCase(newTrain.getTrainId())).findFirst();
        if(existingTrain.isPresent()){
            updateTrain(newTrain);
        }else{
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain){
        int index = -1;
        for(int i=0; i<trainList.size(); i++){
            if(trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId())){
                index = i;
                break;
            }
        }

        if(index != -1){
            trainList.set(index, updatedTrain);
            saveTrainListToFile();
        }else{
            addTrain(updatedTrain);
        }
    }

    public void saveTrainListToFile() {
        try {
            objectMapper.writeValue(new File(TRAIN_DB_PATH),trainList);
        } catch (IOException e) {
            System.out.println("Something went wrong while saving trainList to json file!");
            throw new RuntimeException(e);
        }
    }
}