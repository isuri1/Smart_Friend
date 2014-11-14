/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartfriend.handGesture;

import smartfriend.gui.GUIForm;

/**
 *
 * @author Meuru
 */
public class HandGestureRecongnition {
    
    private GUIForm gUIForm;
    
    public HandGestureRecongnition(){
        gUIForm = new GUIForm();
        gUIForm.setVisible(true);
    }
    
    public static void main(String[] args) {
        new HandGestureRecongnition();
    }
    
}
