package com.raytron.graphics;

/**
 * Popup and Task related UI Handler.
 * 
 * @author Kedar Raybagkar
 *
 */
public interface PopupTaskHandler {

    /**
     * Called when task is completed.
     */
    public abstract void taskCompleted();

    /**
     * Displays an information popup for the given string and window title.
     * 
     * @param message to be displayed
     * @param title of the window
     */
    public abstract void showInformationPopup(Object message, String title);

    /**
     * Displays an error popup for the given string and window title.
     * 
     * @param message to be displayed
     * @param title of the window
     */
    public abstract void showErrorPopup(Object message, String title);

}