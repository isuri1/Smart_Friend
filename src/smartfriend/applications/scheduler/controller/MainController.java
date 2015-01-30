package smartfriend.applications.scheduler.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;
import smartfriend.util.general.MainConfiguration;

/**
 *
 * @author Isuri
 */
public class MainController implements Initializable {

    @FXML
    private Button recordButton;
    @FXML
    private Button savebutton;
    @FXML
    private Slider hourSLider;
    @FXML
    private Slider minuteSlider;
    @FXML
    private Slider repeatSlider;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ToggleButton am;
    @FXML
    private ToggleButton pm;

    private Date date;
    private int hours, minuts, repeat;
    int line_No = 0;
    String am_pm;

    @Override
    public void initialize(URL location, ResourceBundle bundle) {

        recordButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Parent second;
                try {
                    Label secondLabel = new Label("Hello");

                    StackPane secondaryLayout = new StackPane();
                    secondaryLayout.getChildren().add(secondLabel);

                    second = FXMLLoader.load(getClass().getResource("recording.fxml"));
                    Stage stage = new Stage();
                   // stage.getIcons().add(new Image(this.getClass().getResource("s_clock_icon.png").toString()));
                    stage.setTitle("Recording Window");
                    stage.setScene(new Scene(second));
                    stage.show();

                } catch (IOException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        hourSLider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Slider Value Changed (newValue: " + newValue.intValue() + ")");
            hours = newValue.intValue();
        });

        minuteSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Slider Value Changed (newValue: " + newValue.intValue() + ")");
            minuts = newValue.intValue();
        });

        repeatSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Slider Value Changed (newValue: " + newValue.intValue() + ")");
            repeat = newValue.intValue();
        });

        datePicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                LocalDate date1 = datePicker.getValue();
                System.err.println("Selected date: " + date1);

                date = Date.from(date1.atStartOfDay(ZoneId.systemDefault()).toInstant());
                System.out.println("***" + date);
            }
        });

        ToggleGroup am_pm_group = new ToggleGroup();
        am.setToggleGroup(am_pm_group);
        pm.setToggleGroup(am_pm_group);
        am.setUserData("AM");
        pm.setUserData("PM");

        am_pm_group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {

                am_pm = (String) t1.getToggleGroup().getSelectedToggle().getUserData();
            }
        });

        savebutton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    line_No=0;
                    int record_no=getLineNumber() +1;
                    File file = new File(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("recordingFilePath") + record_no + ".wav");
                    if (am_pm == null) {
                        Dialogs.create()
                                .title("Attention!")
                                .masthead(null)
                                .message("Please select am or pm button")
                                .showInformation();
                    }
                    if (date == null) {
                        Dialogs.create()
                                .title("Attention!")
                                .masthead(null)
                                .message("Please select a date")
                                .showInformation();
                    }
                    if(!file.exists()){
                        Dialogs.create()
                                .title("Attention!")
                                .masthead(null)
                                .message("Please record the task to be scheduled")
                                .showInformation();
                    }
                    else {
                        appendToFile();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void appendToFile() {
        try {

            line_No = 0;
            Calendar cal = new GregorianCalendar();
            cal.setTime(date);
            int mm = cal.get(Calendar.MONTH);
            int dd = cal.get(Calendar.DAY_OF_MONTH);
            int yy = cal.get(Calendar.YEAR);
            int waveRecordingNo = getLineNumber() + 1;

            String data = waveRecordingNo + " " + yy + " " + (mm + 1) + " " + dd + " " + hours + " " + minuts + " " + repeat + " " + am_pm;

            File file = new File(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("schedulerFilePath"));

            //if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            //true = append file
            FileWriter fileWritter = new FileWriter(file.getName(), true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.newLine();
            bufferWritter.close();

            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLineNumber() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("schedulerFilePath")));

            while ((br.readLine()) != null) {
                line_No++;
            }
            System.out.println("**line" + line_No);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return line_No;
    }

    public void validateEntry() {

    }

}
