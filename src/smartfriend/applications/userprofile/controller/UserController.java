/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.applications.userprofile.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.controlsfx.dialog.Dialogs;
import smartfriend.util.general.MainConfiguration;

/**
 *
 * @author user
 */
public class UserController implements Initializable {

    @FXML
    private Button SaveDetailsButton;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox yearComboBox;
    @FXML
    private ComboBox monthComboBox;
    @FXML
    private ComboBox dayComboBox;

    String userName, emailAddress, yearSelected, daySelected;
    int monthSelected;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        nameField.setOnMousePressed(new EventHandler() {
            @Override
            public void handle(Event event) {
                try {
                    Runtime.getRuntime().exec("cmd /c C:\\Windows\\System32\\osk.exe");
                } catch (IOException ex) {
                    Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

//        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
//            userName = newValue;
//        });

        emailField.setOnMousePressed(new EventHandler() {
            @Override
            public void handle(Event event) {
                try {
                    Runtime.getRuntime().exec("cmd /c C:\\Windows\\System32\\osk.exe");
                } catch (IOException ex) {
                    Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

//        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
//
//            emailAddress = newValue;
//        });

        SaveDetailsButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (userName == null) {
                    Dialogs.create()
                            .title("Attention!")
                            .masthead(null)
                            .message("Please enter the user name")
                            .showInformation();
                } else if (emailAddress == null) {
                    Dialogs.create()
                            .title("Attention!")
                            .masthead(null)
                            .message("Please enter your email address")
                            .showInformation();
                } else if ((yearSelected == null) || (monthSelected == 0) || (daySelected == null)) {
                    Dialogs.create()
                            .title("Attention!")
                            .masthead(null)
                            .message("Please enter the date of birth of the user")
                            .showInformation();
                } else {
                    appendToFile();
                }
            }
        });

        yearComboBox.getItems().addAll("2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014");
        yearComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                yearSelected = t1;
            }
        });

        monthComboBox.getItems().addAll("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        monthComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
                for (int monthInt = 0; monthInt < months.size(); monthInt++) {
                    if (months.get(monthInt).equals(t1)) {
                        monthSelected = monthInt + 1;
                    }
                }
            }
        });

        dayComboBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31");
        dayComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                daySelected = t1;
            }
        });
    }

    public void appendToFile() {
        try {

            String data = userName + " " + emailAddress + " " + yearSelected + " " + monthSelected + " " + daySelected;
            File file = new File(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("userDetailsFilePath"));

//            File file = new File("C:/Users/user/Documents/NetBeansProjects/FX2/user_details_text.txt");
            //if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            //true = append file
            FileWriter fileWritter = new FileWriter(file.getName(), false);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
//            bufferWritter.newLine();
            bufferWritter.close();

            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
