package smartfriend.applications.userprofile;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import smartfriend.util.general.MainConfiguration;

/**
 *
 * @author Isuri
 */
public class UserDetails {

    private String childName;
    private String email;
    private int age;
    
    public UserDetails(){
        readFile();
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public void setParentEmail(String email) {
        this.email = email;
    }

    public void setChildAge(int age) {
        this.age = age;
    }

    public String getParentEmail() {
        return email;
    }

    public String getChildName() {
        return childName;
    }

    public int getChildAge() {
        return age;
    }

    public void readFile() {

        BufferedReader br = null;
        int year1,month1,day1;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(MainConfiguration.getCurrentDirectory() + MainConfiguration.getInstance().getProperty("userDetailsFilePath")));

            while ((sCurrentLine = br.readLine()) != null) {

                String words[] = sCurrentLine.split(" ");

                setChildName(words[0].trim());
                setParentEmail(words[1].trim());
                year1 = Integer.parseInt(words[2].trim());
                month1= Integer.parseInt(words[3].trim());
                day1= Integer.parseInt(words[4].trim());

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date birthDate = sdf.parse(day1+"/"+month1+"/"+year1); 
                Age age1 = calculateAge(birthDate);
                setChildAge(age1.getYears());
//                System.out.println(getChildName()+" "+getParentEmail()+" "+ getChildAge());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UserDetails.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserDetails.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(UserDetails.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Age calculateAge(Date birthDate) {
        int years = 0;
        int months = 0;
        int days = 0;
        //create calendar object for birth day
        Calendar birthDay = Calendar.getInstance();
        birthDay.setTimeInMillis(birthDate.getTime());
        //create calendar object for current day
        long currentTime = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);
        //Get difference between years
        years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
        int currMonth = now.get(Calendar.MONTH) + 1;
        int birthMonth = birthDay.get(Calendar.MONTH) + 1;
        //Get difference between months
        months = currMonth - birthMonth;
        //if month difference is in negative then reduce years by one and calculate the number of months.
        if (months < 0) {
            years--;
            months = 12 - birthMonth + currMonth;
            if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
                months--;
            }
        } else if (months == 0 && now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            years--;
            months = 11;
        }
        //Calculate the days
        if (now.get(Calendar.DATE) > birthDay.get(Calendar.DATE)) {
            days = now.get(Calendar.DATE) - birthDay.get(Calendar.DATE);
        } else if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            int today = now.get(Calendar.DAY_OF_MONTH);
            now.add(Calendar.MONTH, -1);
            days = now.getActualMaximum(Calendar.DAY_OF_MONTH) - birthDay.get(Calendar.DAY_OF_MONTH) + today;
        } else {
            days = 0;
            if (months == 12) {
                years++;
                months = 0;
            }
        }
        //Create new Age object
        return new Age(days, months, years);
    }
}
