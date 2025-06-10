package org.example.nfldata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;


public class DraftByYearController implements Initializable {

    @FXML
    private ChoiceBox<Integer> yearChoice;

    @FXML
    private ChoiceBox<String> roundChoice;

    @FXML
    private TableView<DraftPlayer2025> draftTable;

    @FXML
    private TableColumn<DraftPlayer2025, String> round;

    @FXML
    private TableColumn<DraftPlayer2025, String> pick;

    @FXML
    private TableColumn<DraftPlayer2025, String> team;

    @FXML
    private TableColumn<DraftPlayer2025, String> name;

    @FXML
    private TableColumn<DraftPlayer2025, String> position;

    @FXML
    private TableColumn<DraftPlayer2025, String> age;

    @FXML
    private TableColumn<DraftPlayer2025, String> college;

    private int year;

    @FXML
    private Label roundLabel;



    public void getYearData(ActionEvent event) {
        try {
            roundChoice.setVisible(true);
            roundLabel.setVisible(true);
            roundChoice.setValue("All Rounds");
            year = yearChoice.getValue();
            File file = new File(Integer.toString(year) + "draft.csv");
            Scanner draftScnr = new Scanner(file);
            String lineOne = draftScnr.nextLine();
            ObservableList<DraftPlayer2025> data = FXCollections.observableArrayList();
            if (year == 2025) {
                while (draftScnr.hasNextLine()) {
                    String line = draftScnr.nextLine();
                    String[] parts = line.split(",");
                    DraftPlayer2025 player = new DraftPlayer2025(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[9]);
                    data.add(player);
                }
            } else {
                while (draftScnr.hasNextLine()) {
                    String line = draftScnr.nextLine();
                    String[] parts = line.split(",");
                    DraftPlayer2025 player = new DraftPlayer2025(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[27]);
                    data.add(player);
                }
            }
            draftTable.setItems(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void getRoundData(ActionEvent event) {
        try {
            String choice = roundChoice.getValue();
            if (choice.equals("All Rounds")) {
                getYearData(event);
            } else {
                File file = new File(Integer.toString(year) + "draft.csv");
                Scanner draftScnr = new Scanner(file);
                String lineOne = draftScnr.nextLine();
                ObservableList<DraftPlayer2025> data = FXCollections.observableArrayList();
                if (year == 2025) {
                    while (draftScnr.hasNextLine()) {
                        String line = draftScnr.nextLine();
                        String[] parts = line.split(",");
                        DraftPlayer2025 player = new DraftPlayer2025(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[9]);
                        if (player.getRound().equals(choice)) {
                            data.add(player);
                        }
                    }
                } else {
                    while (draftScnr.hasNextLine()) {
                        String line = draftScnr.nextLine();
                        String[] parts = line.split(",");
                        DraftPlayer2025 player = new DraftPlayer2025(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[27]);
                        if (player.getRound().equals(choice)) {
                            data.add(player);
                        }
                    }
                }
                draftTable.setItems(data);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int i = 2015; i <= 2025; ++i) {
            yearChoice.getItems().add(i);
        }
        for (int i = 1; i <= 7; ++i) {
            roundChoice.getItems().add(Integer.toString(i));
        }
        roundChoice.getItems().add("All Rounds");
        roundChoice.setVisible(false);
        roundLabel.setVisible(false);
        yearChoice.setOnAction(this::getYearData);
        roundChoice.setOnAction(this::getRoundData);
        round.setCellValueFactory(new PropertyValueFactory<>("round"));
        pick.setCellValueFactory(new PropertyValueFactory<>("pick"));
        team.setCellValueFactory(new PropertyValueFactory<>("team"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        position.setCellValueFactory(new PropertyValueFactory<>("position"));
        age.setCellValueFactory(new PropertyValueFactory<>("age"));
        college.setCellValueFactory(new PropertyValueFactory<>("college"));
        draftTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
