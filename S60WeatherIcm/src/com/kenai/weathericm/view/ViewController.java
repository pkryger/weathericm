/*
 *  Copyright (C) 2010 Przemek Kryger
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.kenai.weathericm.view;

import com.kenai.weathericm.view.validation.NewEditMeteorogramInfoFormData;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import com.kenai.weathericm.util.Status;
import com.kenai.weathericm.app.MeteorogramBroker;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
//#mdebug
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
import net.sf.microlog.midp.appender.FormAppender;
//#enddebug
import org.netbeans.microedition.lcdui.WaitScreen;
import com.kenai.weathericm.app.ForecastDataDownloader;
import com.kenai.weathericm.util.StatusListener;
import com.kenai.weathericm.app.MeteorogramBrokerListener;
import com.kenai.weathericm.domain.Availability;
import com.kenai.weathericm.util.StatusReporter;
import com.kenai.weathericm.domain.MeteorogramInfo;
import com.kenai.weathericm.domain.MeteorogramType;
import com.kenai.weathericm.util.AppConfigurator;
import com.kenai.weathericm.view.validation.MeteorogramInfoDataValidator;
import org.netbeans.microedition.util.CancellableTask;

/**
 * This is the main view controller for the application.
 * @author Przemek Kryger
 */
public class ViewController extends MIDlet implements
        CommandListener, MeteorogramBrokerListener, StatusListener {

    /**
     * The default {@code name} for a new location.
     */
    public final static String DEFAULT_NAME = "name";
    /**
     * The default {@code x} for a new location.
     */
    public final static int DEFAULT_X = 0;
    /**
     * The default {@code y} for a new location.
     */
    public final static int DEFAULT_Y = 0;
    /**
     * The default {@code type} for a new location.
     */
    public final static MeteorogramType DEFAULT_MODEL = MeteorogramType.UM;
    /**
     * The New Location screen name.
     */
    public final static String NEW_LOCATION_TITLE = "New Location";
    /**
     * The Edit Location screen name.
     */
    public final static String EDIT_LOCATION_TITLE = "Edit Location";
//#mdebug
    /**
     * Logger for this class
     */
    private final static Logger log = LoggerFactory.getLogger(ViewController.class);
//#enddebug
    private boolean midletPaused = false;
    /**
     * The {@link MeteorogramBroker} instance this view talks to.
     */
    private MeteorogramBroker broker = null;
    /**
     * The {@link Hashtable} that models the {@link MeteorogramInfo} to
     * {@value #mainList} mapping.
     */
    private Hashtable infoToMainListIndex = new Hashtable(0);
    /**
     * The marker for the New/Edit Location screen. If it is set to
     * {@code true} then the screen is in 'New' mode. If it is set to
     * {@code false} then screen is in 'Edit' mode.
     * @see #prepareEditMode()
     * @see #prepareNewMode()
     * @see #isValid()
     */
    private boolean newMode = true;
    /**
     * The holder for {@code MeteorogramInfo} data that is used by New/Edit
     * Location screen. It is being set before the screen is displayed for an
     * user input. When input is gathered and validation passed then this is
     * populated with screen data.
     * @see #prepareEditMode()
     * @see #prepareNewMode()
     * @see #handleEditAction()
     * @see #handleNewAction()
     */
    private MeteorogramInfo processedInfo = null;
    /**
     * The validation error message. It is set to non-{@value null} when
     * {@value #validateNewEditForm()} fails validation.
     * @see #validateNewEditForm()
     */
    private String validationError = null;
    //<editor-fold defaultstate="collapsed" desc=" Generated Fields ">//GEN-BEGIN:|fields|0|
    private java.util.Hashtable __previousDisplayables = new java.util.Hashtable();
    private Command exitCommand;
    private Command newCommand;
    private Command editCommand;
    private Command showCommand;
    private Command okCommand;
    private Command cancelCommand;
    private Command backCommand;
    private Command deleteCommand;
    private Command logCommand;
    private Command itemCommand;
    private Command reloadCommand;
    private List mainList;
    private Form newEditForm;
    private ChoiceGroup modelChoiceGroup;
    private TextField nameTextField;
    private TextField xTextField;
    private TextField yTextField;
    private WaitScreen downloadWaitScreen;
    private Form logForm;
    private Alert deleteConfirmationAlert;
    private Alert validationErrorAlert;
    private Alert downloadErrorAlert;
    private Alert exitConfirmationAlert;
    private InfoCanvas displayInfoCanvas;
    private Alert reloadAlert;
    private Image forecastAvailableImage;
    private Image downloadImage;
    private Image forecastNotAvailableImage;
    private Image forecastAvailableOldImage;
    //</editor-fold>//GEN-END:|fields|0|

    /**
     * The ViewController constructor.
     */
    public ViewController() {
    }

    //<editor-fold defaultstate="collapsed" desc=" Generated Methods ">//GEN-BEGIN:|methods|0|
    /**
     * Switches a display to previous displayable of the current displayable.
     * The <code>display</code> instance is obtain from the <code>getDisplay</code> method.
     */
    private void switchToPreviousDisplayable() {
        Displayable __currentDisplayable = getDisplay().getCurrent();
        if (__currentDisplayable != null) {
            Displayable __nextDisplayable = (Displayable) __previousDisplayables.get(__currentDisplayable);
            if (__nextDisplayable != null) {
                switchDisplayable(null, __nextDisplayable);
            }
        }
    }
    //</editor-fold>//GEN-END:|methods|0|
    //<editor-fold defaultstate="collapsed" desc=" Generated Method: initialize ">//GEN-BEGIN:|0-initialize|0|0-preInitialize
    /**
     * Initilizes the application.
     * It is called only once when the MIDlet is started. The method is called before the <code>startMIDlet</code> method.
     */
    private void initialize() {//GEN-END:|0-initialize|0|0-preInitialize
        // write pre-initialize user code here
        AppConfigurator.configure();
//#mdebug
        Logger rootLogger = LoggerFactory.getLogger();
        FormAppender formAppender = (FormAppender) rootLogger.getAppender(0);
        //To avoid a bug in FromAppender it needs to be open (have logFrom set)
        //before you can call setLogForm()
        try {
            formAppender.open();
        } catch (IOException ex) {
            log.fatal("Cannot inintiate form appender in order to set it up!");
        }
        formAppender.setLogForm(getLogForm());
//#enddebug
        okCommand = new Command("Ok", Command.OK, 0);//GEN-BEGIN:|0-initialize|1|0-postInitialize
        mainList = new List("ICM Weather", Choice.IMPLICIT);
        mainList.addCommand(getExitCommand());
        mainList.addCommand(getShowCommand());
        mainList.addCommand(getNewCommand());
        mainList.addCommand(getEditCommand());
        mainList.addCommand(getDeleteCommand());
        mainList.addCommand(getLogCommand());
        mainList.setCommandListener(this);
        mainList.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
        mainList.setSelectCommand(getShowCommand());
        mainList.setSelectedFlags(new boolean[] {  });//GEN-END:|0-initialize|1|0-postInitialize
        // write post-initialize user code here
        // This may looks a little bit odd, but this is a workaround for
        // NetBeans debug settings vs. sceen handling.
        boolean hideLog = true;
//#mdebug
        hideLog = false;
//#enddebug
        if (hideLog) {
            mainList.removeCommand(logCommand);
        }
        // The show, edit and delete commands (info related) need to be left in
        // design, so NetBeans knows how to handle them in commandAction, however
        // they are not needed unless there is some info's read. Let's remove
        // them for a while.
        mainList.removeCommand(getShowCommand());
        mainList.removeCommand(getEditCommand());
        mainList.removeCommand(getDeleteCommand());
        broker = MeteorogramBroker.getInstance();
        broker.addListener(this);
        broker.readAllMeteorogramInfos();
    }//GEN-BEGIN:|0-initialize|2|
    //</editor-fold>//GEN-END:|0-initialize|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: startMIDlet ">//GEN-BEGIN:|3-startMIDlet|0|3-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Started point.
     */
    public void startMIDlet() {//GEN-END:|3-startMIDlet|0|3-preAction
        // write pre-action user code here
        switchDisplayable(null, mainList);//GEN-LINE:|3-startMIDlet|1|3-postAction
        // write post-action user code here
    }//GEN-BEGIN:|3-startMIDlet|2|
    //</editor-fold>//GEN-END:|3-startMIDlet|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: resumeMIDlet ">//GEN-BEGIN:|4-resumeMIDlet|0|4-preAction
    /**
     * Performs an action assigned to the Mobile Device - MIDlet Resumed point.
     */
    public void resumeMIDlet() {//GEN-END:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
//GEN-LINE:|4-resumeMIDlet|1|4-postAction
        // write post-action user code here
    }//GEN-BEGIN:|4-resumeMIDlet|2|
    //</editor-fold>//GEN-END:|4-resumeMIDlet|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: switchDisplayable ">//GEN-BEGIN:|5-switchDisplayable|0|5-preSwitch
    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-END:|5-switchDisplayable|0|5-preSwitch
        // write pre-switch user code here
        Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
        Displayable __currentDisplayable = display.getCurrent();
        if (__currentDisplayable != null  &&  nextDisplayable != null) {
            __previousDisplayables.put(nextDisplayable, __currentDisplayable);
        }
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }//GEN-END:|5-switchDisplayable|1|5-postSwitch
        // write post-switch user code here
    }//GEN-BEGIN:|5-switchDisplayable|2|
    //</editor-fold>//GEN-END:|5-switchDisplayable|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: commandAction for Displayables ">//GEN-BEGIN:|7-commandAction|0|7-preCommandAction
    /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
        if (displayable == deleteConfirmationAlert) {//GEN-BEGIN:|7-commandAction|1|148-preAction
            if (command == cancelCommand) {//GEN-END:|7-commandAction|1|148-preAction
                // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|2|148-postAction
                // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|3|149-preAction
                // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|4|149-postAction
                // write post-action user code here
                handleDeleteAction();
            }//GEN-BEGIN:|7-commandAction|5|224-preAction
        } else if (displayable == displayInfoCanvas) {
            if (command == backCommand) {//GEN-END:|7-commandAction|5|224-preAction
                // write pre-action user code here
                switchDisplayable(null, mainList);//GEN-LINE:|7-commandAction|6|224-postAction
                // write post-action user code here
            } else if (command == reloadCommand) {//GEN-LINE:|7-commandAction|7|245-preAction
                // write pre-action user code here
                getDownloadWaitScreen().setText("Downloading... (0% done)");
                ForecastDataDownloader task = broker.getForcedDownloadTask(processedInfo);
                getDownloadWaitScreen().setTask((CancellableTask) task);
                switchDisplayable(null, getDownloadWaitScreen());//GEN-LINE:|7-commandAction|8|245-postAction
                // write post-action user code here
                task.addListener(this);
            }//GEN-BEGIN:|7-commandAction|9|185-preAction
        } else if (displayable == downloadErrorAlert) {
            if (command == okCommand) {//GEN-END:|7-commandAction|9|185-preAction
                // write pre-action user code here
                switchDisplayable(null, mainList);//GEN-LINE:|7-commandAction|10|185-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|11|97-preAction
        } else if (displayable == downloadWaitScreen) {
            if (command == WaitScreen.FAILURE_COMMAND) {//GEN-END:|7-commandAction|11|97-preAction
                // write pre-action user code here
                unregisterAtDownloadTask(false);
                isContinueFailure();//GEN-LINE:|7-commandAction|12|97-postAction
                // write post-action user code here
            } else if (command == WaitScreen.SUCCESS_COMMAND) {//GEN-LINE:|7-commandAction|13|96-preAction
                // write pre-action user code here
                unregisterAtDownloadTask(false);
                isContinueSuccess();//GEN-LINE:|7-commandAction|14|96-postAction
                // write post-action user code here
            } else if (command == cancelCommand) {//GEN-LINE:|7-commandAction|15|169-preAction
                // write pre-action user code here
                unregisterAtDownloadTask(true);
                switchDisplayable(null, mainList);//GEN-LINE:|7-commandAction|16|169-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|17|209-preAction
        } else if (displayable == exitConfirmationAlert) {
            if (command == cancelCommand) {//GEN-END:|7-commandAction|17|209-preAction
                // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|18|209-postAction
                // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|19|208-preAction
                // write pre-action user code here
                exitMIDlet();//GEN-LINE:|7-commandAction|20|208-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|21|113-preAction
        } else if (displayable == logForm) {
            if (command == backCommand) {//GEN-END:|7-commandAction|21|113-preAction
                // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|7-commandAction|22|113-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|23|24-preAction
        } else if (displayable == mainList) {
            if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|23|24-preAction
                // write pre-action user code here
                mainListAction();//GEN-LINE:|7-commandAction|24|24-postAction
                // write post-action user code here
            } else if (command == deleteCommand) {//GEN-LINE:|7-commandAction|25|119-preAction
                // write pre-action user code here
                prepareProcessedInfo();
                switchDisplayable(null, getDeleteConfirmationAlert());//GEN-LINE:|7-commandAction|26|119-postAction
                // write post-action user code here
            } else if (command == editCommand) {//GEN-LINE:|7-commandAction|27|70-preAction
                // write pre-action user code here
                prepareEditMode();
                switchDisplayable(null, getNewEditForm());//GEN-LINE:|7-commandAction|28|70-postAction
                // write post-action user code here
            } else if (command == exitCommand) {//GEN-LINE:|7-commandAction|29|28-preAction
                // write pre-action user code here
                switchDisplayable(null, getExitConfirmationAlert());//GEN-LINE:|7-commandAction|30|28-postAction
                // write post-action user code here
            } else if (command == logCommand) {//GEN-LINE:|7-commandAction|31|115-preAction
                // write pre-action user code here
                switchDisplayable(null, getLogForm());//GEN-LINE:|7-commandAction|32|115-postAction
                // write post-action user code here
            } else if (command == newCommand) {//GEN-LINE:|7-commandAction|33|46-preAction
                // write pre-action user code here
                prepareNewMode();
                switchDisplayable(null, getNewEditForm());//GEN-LINE:|7-commandAction|34|46-postAction
                // write post-action user code here
            } else if (command == showCommand) {//GEN-LINE:|7-commandAction|35|68-preAction
                // write pre-action user code here
                prepareProcessedInfo();
                isForecastAvaliable();//GEN-LINE:|7-commandAction|36|68-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|37|109-preAction
        } else if (displayable == newEditForm) {
            if (command == cancelCommand) {//GEN-END:|7-commandAction|37|109-preAction
                // write pre-action user code here
                switchDisplayable(null, mainList);//GEN-LINE:|7-commandAction|38|109-postAction
                // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|39|108-preAction
                // write pre-action user code here
                isUserDataValid();//GEN-LINE:|7-commandAction|40|108-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|41|239-preAction
        } else if (displayable == reloadAlert) {
            if (command == cancelCommand) {//GEN-END:|7-commandAction|41|239-preAction
                // write pre-action user code here
                getDisplayInfoCanvas().setInfo(processedInfo);
                switchDisplayable(null, getDisplayInfoCanvas());//GEN-LINE:|7-commandAction|42|239-postAction
                // write post-action user code here
            } else if (command == okCommand) {//GEN-LINE:|7-commandAction|43|238-preAction
                // write pre-action user code here
                getDownloadWaitScreen().setText("Downloading... (0% done)");
                ForecastDataDownloader task = broker.getCheckedDownloadTask(processedInfo);
                getDownloadWaitScreen().setTask((CancellableTask) task);
                switchDisplayable(null, getDownloadWaitScreen());//GEN-LINE:|7-commandAction|44|238-postAction
                // write post-action user code here
                task.addListener(this);
            }//GEN-BEGIN:|7-commandAction|45|166-preAction
        } else if (displayable == validationErrorAlert) {
            if (command == okCommand) {//GEN-END:|7-commandAction|45|166-preAction
                // write pre-action user code here
                switchDisplayable(null, getNewEditForm());//GEN-LINE:|7-commandAction|46|166-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|47|7-postCommandAction
        }//GEN-END:|7-commandAction|47|7-postCommandAction
        // write post-action user code here
    }//GEN-BEGIN:|7-commandAction|48|
    //</editor-fold>//GEN-END:|7-commandAction|48|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitCommand ">//GEN-BEGIN:|18-getter|0|18-preInit
    /**
     * Returns an initiliazed instance of exitCommand component.
     * @return the initialized component instance
     */
    public Command getExitCommand() {
        if (exitCommand == null) {//GEN-END:|18-getter|0|18-preInit
            // write pre-init user code here
            exitCommand = new Command("Exit", Command.EXIT, 0);//GEN-LINE:|18-getter|1|18-postInit
            // write post-init user code here
        }//GEN-BEGIN:|18-getter|2|
        return exitCommand;
    }
    //</editor-fold>//GEN-END:|18-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: mainListAction ">//GEN-BEGIN:|22-action|0|22-preAction
    /**
     * Performs an action assigned to the selected list element in the mainList component.
     */
    public void mainListAction() {//GEN-END:|22-action|0|22-preAction
        // enter pre-action user code here
        String __selectedString = mainList.getString(mainList.getSelectedIndex());//GEN-LINE:|22-action|1|22-postAction
        // enter post-action user code here
    }//GEN-BEGIN:|22-action|2|
    //</editor-fold>//GEN-END:|22-action|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: newCommand ">//GEN-BEGIN:|45-getter|0|45-preInit
    /**
     * Returns an initiliazed instance of newCommand component.
     * @return the initialized component instance
     */
    public Command getNewCommand() {
        if (newCommand == null) {//GEN-END:|45-getter|0|45-preInit
            // write pre-init user code here
            newCommand = new Command("New Location", Command.SCREEN, 0);//GEN-LINE:|45-getter|1|45-postInit
            // write post-init user code here
        }//GEN-BEGIN:|45-getter|2|
        return newCommand;
    }
    //</editor-fold>//GEN-END:|45-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: newEditForm ">//GEN-BEGIN:|47-getter|0|47-preInit
    /**
     * Returns an initiliazed instance of newEditForm component.
     * @return the initialized component instance
     */
    public Form getNewEditForm() {
        if (newEditForm == null) {//GEN-END:|47-getter|0|47-preInit
            // write pre-init user code here
            newEditForm = new Form("", new Item[] { getNameTextField(), getXTextField(), getYTextField(), getModelChoiceGroup() });//GEN-BEGIN:|47-getter|1|47-postInit
            newEditForm.addCommand(okCommand);
            newEditForm.addCommand(getCancelCommand());
            newEditForm.setCommandListener(this);//GEN-END:|47-getter|1|47-postInit
            // write post-init user code here
        }//GEN-BEGIN:|47-getter|2|
        return newEditForm;
    }
    //</editor-fold>//GEN-END:|47-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: modelChoiceGroup ">//GEN-BEGIN:|61-getter|0|61-preInit
    /**
     * Returns an initiliazed instance of modelChoiceGroup component.
     * @return the initialized component instance
     */
    public ChoiceGroup getModelChoiceGroup() {
        if (modelChoiceGroup == null) {//GEN-END:|61-getter|0|61-preInit
            // write pre-init user code here
            modelChoiceGroup = new ChoiceGroup("Model", Choice.EXCLUSIVE);//GEN-BEGIN:|61-getter|1|61-postInit
            modelChoiceGroup.append("UM", null);
            modelChoiceGroup.append("COAMPS", null);
            modelChoiceGroup.setSelectedFlags(new boolean[] { false, false });//GEN-END:|61-getter|1|61-postInit
            // write post-init user code here
        }//GEN-BEGIN:|61-getter|2|
        return modelChoiceGroup;
    }
    //</editor-fold>//GEN-END:|61-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: nameTextField ">//GEN-BEGIN:|64-getter|0|64-preInit
    /**
     * Returns an initiliazed instance of nameTextField component.
     * @return the initialized component instance
     */
    public TextField getNameTextField() {
        if (nameTextField == null) {//GEN-END:|64-getter|0|64-preInit
            // write pre-init user code here
            nameTextField = new TextField("Name", "", MeteorogramInfoDataValidator.MAX_NAME_LENGTH, TextField.ANY | TextField.NON_PREDICTIVE | TextField.INITIAL_CAPS_WORD | TextField.INITIAL_CAPS_SENTENCE);//GEN-BEGIN:|64-getter|1|64-postInit
            nameTextField.setInitialInputMode("UCB_BASIC_LATIN");//GEN-END:|64-getter|1|64-postInit
            // write post-init user code here
        }//GEN-BEGIN:|64-getter|2|
        return nameTextField;
    }
    //</editor-fold>//GEN-END:|64-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: xTextField ">//GEN-BEGIN:|65-getter|0|65-preInit
    /**
     * Returns an initiliazed instance of xTextField component.
     * @return the initialized component instance
     */
    public TextField getXTextField() {
        if (xTextField == null) {//GEN-END:|65-getter|0|65-preInit
            // write pre-init user code here
            xTextField = new TextField("X", "0", 3, TextField.NUMERIC | TextField.NON_PREDICTIVE);//GEN-BEGIN:|65-getter|1|65-postInit
            xTextField.setInitialInputMode("UCB_BASIC_LATIN");//GEN-END:|65-getter|1|65-postInit
            // write post-init user code here
        }//GEN-BEGIN:|65-getter|2|
        return xTextField;
    }
    //</editor-fold>//GEN-END:|65-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: yTextField ">//GEN-BEGIN:|66-getter|0|66-preInit
    /**
     * Returns an initiliazed instance of yTextField component.
     * @return the initialized component instance
     */
    public TextField getYTextField() {
        if (yTextField == null) {//GEN-END:|66-getter|0|66-preInit
            // write pre-init user code here
            yTextField = new TextField("Y", "0", 3, TextField.NUMERIC);//GEN-BEGIN:|66-getter|1|66-postInit
            yTextField.setInitialInputMode("UCB_BASIC_LATIN");//GEN-END:|66-getter|1|66-postInit
            // write post-init user code here
        }//GEN-BEGIN:|66-getter|2|
        return yTextField;
    }
    //</editor-fold>//GEN-END:|66-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: showCommand ">//GEN-BEGIN:|67-getter|0|67-preInit
    /**
     * Returns an initiliazed instance of showCommand component.
     * @return the initialized component instance
     */
    public Command getShowCommand() {
        if (showCommand == null) {//GEN-END:|67-getter|0|67-preInit
            // write pre-init user code here
            showCommand = new Command("Show Forecast", Command.ITEM, 0);//GEN-LINE:|67-getter|1|67-postInit
            // write post-init user code here
        }//GEN-BEGIN:|67-getter|2|
        return showCommand;
    }
    //</editor-fold>//GEN-END:|67-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: editCommand ">//GEN-BEGIN:|69-getter|0|69-preInit
    /**
     * Returns an initiliazed instance of editCommand component.
     * @return the initialized component instance
     */
    public Command getEditCommand() {
        if (editCommand == null) {//GEN-END:|69-getter|0|69-preInit
            // write pre-init user code here
            editCommand = new Command("Edit Location", Command.ITEM, 1);//GEN-LINE:|69-getter|1|69-postInit
            // write post-init user code here
        }//GEN-BEGIN:|69-getter|2|
        return editCommand;
    }
    //</editor-fold>//GEN-END:|69-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: cancelCommand ">//GEN-BEGIN:|75-getter|0|75-preInit
    /**
     * Returns an initiliazed instance of cancelCommand component.
     * @return the initialized component instance
     */
    public Command getCancelCommand() {
        if (cancelCommand == null) {//GEN-END:|75-getter|0|75-preInit
            // write pre-init user code here
            cancelCommand = new Command("Cancel", Command.CANCEL, 0);//GEN-LINE:|75-getter|1|75-postInit
            // write post-init user code here
        }//GEN-BEGIN:|75-getter|2|
        return cancelCommand;
    }
    //</editor-fold>//GEN-END:|75-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: backCommand ">//GEN-BEGIN:|88-getter|0|88-preInit
    /**
     * Returns an initiliazed instance of backCommand component.
     * @return the initialized component instance
     */
    public Command getBackCommand() {
        if (backCommand == null) {//GEN-END:|88-getter|0|88-preInit
            // write pre-init user code here
            backCommand = new Command("Back", Command.BACK, 0);//GEN-LINE:|88-getter|1|88-postInit
            // write post-init user code here
        }//GEN-BEGIN:|88-getter|2|
        return backCommand;
    }
    //</editor-fold>//GEN-END:|88-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isForecastAvaliable ">//GEN-BEGIN:|100-if|0|100-preIf
    /**
     * Performs an action assigned to the isForecastAvaliable if-point.
     */
    public void isForecastAvaliable() {//GEN-END:|100-if|0|100-preIf
        // enter pre-if user code here
        if (processedInfo.dataAvailability().equals(Availability.AVAILABLE)) {//GEN-LINE:|100-if|1|101-preAction
            // write pre-action user code here
            getDisplayInfoCanvas().setInfo(processedInfo);
            switchDisplayable(null, getDisplayInfoCanvas());//GEN-LINE:|100-if|2|101-postAction
            // write post-action user code here
        } else {//GEN-LINE:|100-if|3|102-preAction
            // write pre-action user code here
            isForecastAvailableOld();//GEN-LINE:|100-if|4|102-postAction
            // write post-action user code here
        }//GEN-LINE:|100-if|5|100-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|100-if|6|
    //</editor-fold>//GEN-END:|100-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: downloadWaitScreen ">//GEN-BEGIN:|93-getter|0|93-preInit
    /**
     * Returns an initiliazed instance of downloadWaitScreen component.
     * @return the initialized component instance
     */
    public WaitScreen getDownloadWaitScreen() {
        if (downloadWaitScreen == null) {//GEN-END:|93-getter|0|93-preInit
            // write pre-init user code here
            downloadWaitScreen = new WaitScreen(getDisplay());//GEN-BEGIN:|93-getter|1|93-postInit
            downloadWaitScreen.setTitle("Downloading Meteorogram");
            downloadWaitScreen.addCommand(getCancelCommand());
            downloadWaitScreen.setCommandListener(this);
            downloadWaitScreen.setImage(getDownloadImage());//GEN-END:|93-getter|1|93-postInit
            // write post-init user code here
        }//GEN-BEGIN:|93-getter|2|
        return downloadWaitScreen;
    }
    //</editor-fold>//GEN-END:|93-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logCommand ">//GEN-BEGIN:|114-getter|0|114-preInit
    /**
     * Returns an initiliazed instance of logCommand component.
     * @return the initialized component instance
     */
    public Command getLogCommand() {
        if (logCommand == null) {//GEN-END:|114-getter|0|114-preInit
            // write pre-init user code here
//#mdebug
            logCommand = new Command("Log", Command.SCREEN, 100);//GEN-LINE:|114-getter|1|114-postInit
            // write post-init user code here
//#enddebug
        }//GEN-BEGIN:|114-getter|2|
        return logCommand;
    }
    //</editor-fold>//GEN-END:|114-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: logForm ">//GEN-BEGIN:|112-getter|0|112-preInit
    /**
     * Returns an initiliazed instance of logForm component.
     * @return the initialized component instance
     */
    public Form getLogForm() {
        if (logForm == null) {//GEN-END:|112-getter|0|112-preInit
            // write pre-init user code here
//#mdebug
            logForm = new Form("Log");//GEN-BEGIN:|112-getter|1|112-postInit
            logForm.addCommand(getBackCommand());
            logForm.setCommandListener(this);//GEN-END:|112-getter|1|112-postInit
            // write post-init user code here
//#enddebug
        }//GEN-BEGIN:|112-getter|2|
        return logForm;
    }
    //</editor-fold>//GEN-END:|112-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: deleteCommand ">//GEN-BEGIN:|118-getter|0|118-preInit
    /**
     * Returns an initiliazed instance of deleteCommand component.
     * @return the initialized component instance
     */
    public Command getDeleteCommand() {
        if (deleteCommand == null) {//GEN-END:|118-getter|0|118-preInit
            // write pre-init user code here
            deleteCommand = new Command("Delete Location", Command.ITEM, 2);//GEN-LINE:|118-getter|1|118-postInit
            // write post-init user code here
        }//GEN-BEGIN:|118-getter|2|
        return deleteCommand;
    }
    //</editor-fold>//GEN-END:|118-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isUserDataValid ">//GEN-BEGIN:|126-if|0|126-preIf
    /**
     * Performs an action assigned to the isUserDataValid if-point.
     */
    public void isUserDataValid() {//GEN-END:|126-if|0|126-preIf
        // enter pre-if user code here
        if (validateNewEditForm()) {//GEN-LINE:|126-if|1|127-preAction
            // write pre-action user code here
//#mdebug
            log.info("Validation passed for " + processedInfo);
//#enddebug
            switchDisplayable(null, mainList);//GEN-LINE:|126-if|2|127-postAction
            // write post-action user code here
            if (newMode == true) {
                handleNewAction();
            } else {
                handleEditAction();
            }
        } else {//GEN-LINE:|126-if|3|128-preAction
            // write pre-action user code here
            // make sure alert has the correct tex to display
            if (validationErrorAlert != null) {
                validationErrorAlert.setString(validationError);
            }
//#mdebug
            log.info("Validation failed for " + processedInfo);
//#enddebug
            switchDisplayable(null, getValidationErrorAlert());//GEN-LINE:|126-if|4|128-postAction
            // write post-action user code here
        }//GEN-LINE:|126-if|5|126-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|126-if|6|
    //</editor-fold>//GEN-END:|126-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: deleteConfirmationAlert ">//GEN-BEGIN:|143-getter|0|143-preInit
    /**
     * Returns an initiliazed instance of deleteConfirmationAlert component.
     * @return the initialized component instance
     */
    public Alert getDeleteConfirmationAlert() {
        if (deleteConfirmationAlert == null) {//GEN-END:|143-getter|0|143-preInit
            // write pre-init user code here
            deleteConfirmationAlert = new Alert("Delete Confirmation", "Are you sure you want to delete the selected location?", null, AlertType.CONFIRMATION);//GEN-BEGIN:|143-getter|1|143-postInit
            deleteConfirmationAlert.addCommand(getCancelCommand());
            deleteConfirmationAlert.addCommand(okCommand);
            deleteConfirmationAlert.setCommandListener(this);
            deleteConfirmationAlert.setTimeout(Alert.FOREVER);//GEN-END:|143-getter|1|143-postInit
            // write post-init user code here
        }//GEN-BEGIN:|143-getter|2|
        return deleteConfirmationAlert;
    }
    //</editor-fold>//GEN-END:|143-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: validationErrorAlert ">//GEN-BEGIN:|156-getter|0|156-preInit
    /**
     * Returns an initiliazed instance of validationErrorAlert component.
     * @return the initialized component instance
     */
    public Alert getValidationErrorAlert() {
        if (validationErrorAlert == null) {//GEN-END:|156-getter|0|156-preInit
            // write pre-init user code here
            validationErrorAlert = new Alert(null, validationError, null, AlertType.ERROR);//GEN-BEGIN:|156-getter|1|156-postInit
            validationErrorAlert.addCommand(okCommand);
            validationErrorAlert.setCommandListener(this);
            validationErrorAlert.setTimeout(3000);//GEN-END:|156-getter|1|156-postInit
            // write post-init user code here
        }//GEN-BEGIN:|156-getter|2|
        return validationErrorAlert;
    }
    //</editor-fold>//GEN-END:|156-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: downloadErrorAlert ">//GEN-BEGIN:|161-getter|0|161-preInit
    /**
     * Returns an initiliazed instance of downloadErrorAlert component.
     * @return the initialized component instance
     */
    public Alert getDownloadErrorAlert() {
        if (downloadErrorAlert == null) {//GEN-END:|161-getter|0|161-preInit
            // write pre-init user code here
            downloadErrorAlert = new Alert(null, "An error occurred while downloading forecast!", null, AlertType.ERROR);//GEN-BEGIN:|161-getter|1|161-postInit
            downloadErrorAlert.addCommand(okCommand);
            downloadErrorAlert.setCommandListener(this);
            downloadErrorAlert.setTimeout(3000);//GEN-END:|161-getter|1|161-postInit
            // write post-init user code here
        }//GEN-BEGIN:|161-getter|2|
        return downloadErrorAlert;
    }
    //</editor-fold>//GEN-END:|161-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isContinueSuccess ">//GEN-BEGIN:|175-if|0|175-preIf
    /**
     * Performs an action assigned to the isContinueSuccess if-point.
     */
    public void isContinueSuccess() {//GEN-END:|175-if|0|175-preIf
        // enter pre-if user code here
        if (isDownloadWaitScreenVisible()) {//GEN-LINE:|175-if|1|176-preAction
            // write pre-action user code here
            getDisplayInfoCanvas().setInfo(processedInfo);
            switchDisplayable(null, getDisplayInfoCanvas());//GEN-LINE:|175-if|2|176-postAction
            // write post-action user code here
        } else {//GEN-LINE:|175-if|3|177-preAction
            // write pre-action user code here
//GEN-LINE:|175-if|4|177-postAction
            // write post-action user code here
        }//GEN-LINE:|175-if|5|175-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|175-if|6|
    //</editor-fold>//GEN-END:|175-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isContinueFailure ">//GEN-BEGIN:|180-if|0|180-preIf
    /**
     * Performs an action assigned to the isContinueFailure if-point.
     */
    public void isContinueFailure() {//GEN-END:|180-if|0|180-preIf
        // enter pre-if user code here
        if (isDownloadWaitScreenVisible()) {//GEN-LINE:|180-if|1|181-preAction
            // write pre-action user code here
            switchDisplayable(null, getDownloadErrorAlert());//GEN-LINE:|180-if|2|181-postAction
            // write post-action user code here
        } else {//GEN-LINE:|180-if|3|182-preAction
            // write pre-action user code here
//GEN-LINE:|180-if|4|182-postAction
            // write post-action user code here
        }//GEN-LINE:|180-if|5|180-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|180-if|6|
    //</editor-fold>//GEN-END:|180-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: exitConfirmationAlert ">//GEN-BEGIN:|207-getter|0|207-preInit
    /**
     * Returns an initiliazed instance of exitConfirmationAlert component.
     * @return the initialized component instance
     */
    public Alert getExitConfirmationAlert() {
        if (exitConfirmationAlert == null) {//GEN-END:|207-getter|0|207-preInit
            // write pre-init user code here
            exitConfirmationAlert = new Alert(null, "Exit application?", null, AlertType.CONFIRMATION);//GEN-BEGIN:|207-getter|1|207-postInit
            exitConfirmationAlert.addCommand(okCommand);
            exitConfirmationAlert.addCommand(getCancelCommand());
            exitConfirmationAlert.setCommandListener(this);
            exitConfirmationAlert.setTimeout(Alert.FOREVER);//GEN-END:|207-getter|1|207-postInit
            // write post-init user code here
        }//GEN-BEGIN:|207-getter|2|
        return exitConfirmationAlert;
    }
    //</editor-fold>//GEN-END:|207-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: downloadImage ">//GEN-BEGIN:|214-getter|0|214-preInit
    /**
     * Returns an initiliazed instance of downloadImage component.
     * @return the initialized component instance
     */
    public Image getDownloadImage() {
        if (downloadImage == null) {//GEN-END:|214-getter|0|214-preInit
            // write pre-init user code here
            try {//GEN-BEGIN:|214-getter|1|214-@java.io.IOException
                downloadImage = Image.createImage("/com/kenai/weathericm/images/Gnome-image-loading.png");
            } catch (java.io.IOException e) {//GEN-END:|214-getter|1|214-@java.io.IOException
//#mdebug
                log.error("Cannod get the download image!", e);
//#enddebug
            }//GEN-LINE:|214-getter|2|214-postInit
            // write post-init user code here
        }//GEN-BEGIN:|214-getter|3|
        return downloadImage;
    }
    //</editor-fold>//GEN-END:|214-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: forecastNotAvailableImage ">//GEN-BEGIN:|215-getter|0|215-preInit
    /**
     * Returns an initiliazed instance of forecastNotAvailableImage component.
     * @return the initialized component instance
     */
    public Image getForecastNotAvailableImage() {
        if (forecastNotAvailableImage == null) {//GEN-END:|215-getter|0|215-preInit
            // write pre-init user code here
            try {//GEN-BEGIN:|215-getter|1|215-@java.io.IOException
                forecastNotAvailableImage = Image.createImage("/com/kenai/weathericm/images/Gnome-weather-clear-night.png");
            } catch (java.io.IOException e) {//GEN-END:|215-getter|1|215-@java.io.IOException
//#mdebug
                log.error("Cannot get the image for forecast unavaliable!", e);
//#enddebug
            }//GEN-LINE:|215-getter|2|215-postInit
            // write post-init user code here
        }//GEN-BEGIN:|215-getter|3|
        return forecastNotAvailableImage;
    }
    //</editor-fold>//GEN-END:|215-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: forecastAvailableImage ">//GEN-BEGIN:|216-getter|0|216-preInit
    /**
     * Returns an initiliazed instance of forecastAvailableImage component.
     * @return the initialized component instance
     */
    public Image getForecastAvailableImage() {
        if (forecastAvailableImage == null) {//GEN-END:|216-getter|0|216-preInit
            // write pre-init user code here
            try {//GEN-BEGIN:|216-getter|1|216-@java.io.IOException
                forecastAvailableImage = Image.createImage("/com/kenai/weathericm/images/Gnome-weather-clear.png");
            } catch (java.io.IOException e) {//GEN-END:|216-getter|1|216-@java.io.IOException
//#mdebug
                log.error("Cannot get the image for forecast avaliable!", e);
//#enddebug
            }//GEN-LINE:|216-getter|2|216-postInit
            // write post-init user code here
        }//GEN-BEGIN:|216-getter|3|
        return forecastAvailableImage;
    }
    //</editor-fold>//GEN-END:|216-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: displayInfoCanvas ">//GEN-BEGIN:|222-getter|0|222-preInit
    /**
     * Returns an initiliazed instance of displayInfoCanvas component.
     * @return the initialized component instance
     */
    public InfoCanvas getDisplayInfoCanvas() {
        if (displayInfoCanvas == null) {//GEN-END:|222-getter|0|222-preInit
            // write pre-init user code here
            displayInfoCanvas = new InfoCanvas();//GEN-BEGIN:|222-getter|1|222-postInit
            displayInfoCanvas.addCommand(getBackCommand());
            displayInfoCanvas.addCommand(getReloadCommand());
            displayInfoCanvas.setCommandListener(this);//GEN-END:|222-getter|1|222-postInit
            // write post-init user code here
            displayInfoCanvas.setScrollSpeed(25);
        }//GEN-BEGIN:|222-getter|2|
        return displayInfoCanvas;
    }
    //</editor-fold>//GEN-END:|222-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: forecastAvailableOldImage ">//GEN-BEGIN:|227-getter|0|227-preInit
    /**
     * Returns an initiliazed instance of forecastAvailableOldImage component.
     * @return the initialized component instance
     */
    public Image getForecastAvailableOldImage() {
        if (forecastAvailableOldImage == null) {//GEN-END:|227-getter|0|227-preInit
            // write pre-init user code here
            try {//GEN-BEGIN:|227-getter|1|227-@java.io.IOException
                forecastAvailableOldImage = Image.createImage("/com/kenai/weathericm/images/Gnome-weather-clear-night-day.png");
            } catch (java.io.IOException e) {//GEN-END:|227-getter|1|227-@java.io.IOException
                e.printStackTrace();
            }//GEN-LINE:|227-getter|2|227-postInit
            // write post-init user code here
        }//GEN-BEGIN:|227-getter|3|
        return forecastAvailableOldImage;
    }
    //</editor-fold>//GEN-END:|227-getter|3|

    //<editor-fold defaultstate="collapsed" desc=" Generated Method: isForecastAvailableOld ">//GEN-BEGIN:|229-if|0|229-preIf
    /**
     * Performs an action assigned to the isForecastAvailableOld if-point.
     */
    public void isForecastAvailableOld() {//GEN-END:|229-if|0|229-preIf
        // enter pre-if user code here
        if (processedInfo.dataAvailability().equals(Availability.AVAILABLE_OLD)) {//GEN-LINE:|229-if|1|230-preAction
            // write pre-action user code here
            switchDisplayable(null, getReloadAlert());//GEN-LINE:|229-if|2|230-postAction
            // write post-action user code here
        } else {//GEN-LINE:|229-if|3|231-preAction
            // write pre-action user code here
            getDownloadWaitScreen().setText("Downloading... (0% done)");
            ForecastDataDownloader task = broker.getForcedDownloadTask(processedInfo);
            getDownloadWaitScreen().setTask((CancellableTask) task);
            switchDisplayable(null, getDownloadWaitScreen());//GEN-LINE:|229-if|4|231-postAction
            // write post-action user code here
            task.addListener(this);
        }//GEN-LINE:|229-if|5|229-postIf
        // enter post-if user code here
    }//GEN-BEGIN:|229-if|6|
    //</editor-fold>//GEN-END:|229-if|6|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: reloadAlert ">//GEN-BEGIN:|234-getter|0|234-preInit
    /**
     * Returns an initiliazed instance of reloadAlert component.
     * @return the initialized component instance
     */
    public Alert getReloadAlert() {
        if (reloadAlert == null) {//GEN-END:|234-getter|0|234-preInit
            // write pre-init user code here
            reloadAlert = new Alert(null, "Do you want to check if new version of forecast exist on server?", null, AlertType.CONFIRMATION);//GEN-BEGIN:|234-getter|1|234-postInit
            reloadAlert.addCommand(okCommand);
            reloadAlert.addCommand(getCancelCommand());
            reloadAlert.setCommandListener(this);
            reloadAlert.setTimeout(Alert.FOREVER);//GEN-END:|234-getter|1|234-postInit
            // write post-init user code here
        }//GEN-BEGIN:|234-getter|2|
        return reloadAlert;
    }
    //</editor-fold>//GEN-END:|234-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: itemCommand ">//GEN-BEGIN:|242-getter|0|242-preInit
    /**
     * Returns an initiliazed instance of itemCommand component.
     * @return the initialized component instance
     */
    public Command getItemCommand() {
        if (itemCommand == null) {//GEN-END:|242-getter|0|242-preInit
            // write pre-init user code here
            itemCommand = new Command("Item", Command.SCREEN, 0);//GEN-LINE:|242-getter|1|242-postInit
            // write post-init user code here
        }//GEN-BEGIN:|242-getter|2|
        return itemCommand;
    }
    //</editor-fold>//GEN-END:|242-getter|2|

    //<editor-fold defaultstate="collapsed" desc=" Generated Getter: reloadCommand ">//GEN-BEGIN:|244-getter|0|244-preInit
    /**
     * Returns an initiliazed instance of reloadCommand component.
     * @return the initialized component instance
     */
    public Command getReloadCommand() {
        if (reloadCommand == null) {//GEN-END:|244-getter|0|244-preInit
            // write pre-init user code here
            reloadCommand = new Command("Refresh", Command.SCREEN, 0);//GEN-LINE:|244-getter|1|244-postInit
            // write post-init user code here
        }//GEN-BEGIN:|244-getter|2|
        return reloadCommand;
    }
    //</editor-fold>//GEN-END:|244-getter|2|

    /**
     * Gets the info from {@value #infoToMainListIndex} based on the
     * {@value #mainList} selection.
     */
    protected void prepareProcessedInfo() {
//#mdebug
        log.info("Preparing the selected info to process");
//#enddebug
        processedInfo = getSelectedMeteorogramInfo();
        if (processedInfo == null) {
//#mdebug
            log.fatal("Cannot find a info for current selection!");
//#enddebug
            throw new NullPointerException("Model doesn't contain displayed element!");
        }
    }

    /**
     * Creates new {@link MeteorogramInfo}, then switches the New/Edit Location
     * screen to New mode.
     */
    protected void prepareNewMode() {
//#mdebug
        log.info("Prepraring new location screen");
//#enddebug
        newMode = true;
        getNewEditForm().setTitle(NEW_LOCATION_TITLE);
        processedInfo = new MeteorogramInfo();
        processedInfo.setName(DEFAULT_NAME);
        processedInfo.setX(DEFAULT_X);
        processedInfo.setY(DEFAULT_Y);
        processedInfo.setType(DEFAULT_MODEL);
        populateNewEditForm();
    }

    /**
     * Gets the info from {@value #infoToMainListIndex} based on the
     * {@value #mainList} selection, then switches the New/Edit screen
     * to Edit mode.
     */
    protected void prepareEditMode() {
//#mdebug
        log.info("Preparing edit location screen");
//#enddebug
        newMode = false;
        getNewEditForm().setTitle(EDIT_LOCATION_TITLE);
        processedInfo = getSelectedMeteorogramInfo();
        if (processedInfo == null) {
//#mdebug
            log.fatal("Cannot find a info for current selection!");
//#enddebug
            throw new NullPointerException("Model doesn't contain displayed element!");
        } else {
            populateNewEditForm();
        }
    }

    /**
     * Populates New/Edit Location screen with the {@value #processedInfo} data.
     */
    private void populateNewEditForm() {
        getNameTextField().setString(processedInfo.getName());
        getXTextField().setString(Integer.toString(processedInfo.getX()));
        getYTextField().setString(Integer.toString(processedInfo.getY()));
        getModelChoiceGroup().setSelectedIndex(processedInfo.getType().getValue(), true);
    }

    /**
     * Searches in {@value #infoToMainListIndex} for the {@link MeteorogramInfo}
     * that is currently selected in {@value #mainList}.
     * @return the {@link MeteorogramInfo} that is selected, or {@code null} if
     *         nothing is selected
     */
    private MeteorogramInfo getSelectedMeteorogramInfo() {
        MeteorogramInfo selected = null;
        Integer index = new Integer(mainList.getSelectedIndex());
        Enumeration keys = infoToMainListIndex.keys();
        while (keys.hasMoreElements()) {
            MeteorogramInfo info = (MeteorogramInfo) keys.nextElement();
            if (index.equals(infoToMainListIndex.get(info))) {
                selected = info;
                break;
            }
        }
//#mdebug
        log.info("Returning selected info = " + selected);
//#enddebug
        return selected;
    }

    /**
     * Validates the New/Edit Location screen data and sets up the 
     * {@value #validationError}.
     * @return {@code true} if data in the New/Edit Location screen can be safely
     *         stored and {@value #processedInfo} info is populated with data.
     *         {@code false} if data in the New/Edit Location screen
     *         failed validation and {@value #validationError} is set to the
     *         error message.
     * @see #isValid()
     * @see #handleValidationError()
     */
    protected boolean validateNewEditForm() {
//#mdebug
        log.info("Validating New/Edit Location form data...");
//#enddebug
        validationError = null;
        NewEditMeteorogramInfoFormData data = getNewEditFormData();
        MeteorogramInfoDataValidator validator = new MeteorogramInfoDataValidator();
        validator.validate(data);
        Vector errors = validator.getErrors();
        if (errors == null) {
//#mdebug
            log.info("Validation passed");
//#enddebug
            populateProcessedInfo(data);
            return true;
        } else {
//#mdebug
            log.info("Validataion failed");
//#enddebug
            StringBuffer errorsBuffer = new StringBuffer("Errors: ");
            Enumeration e = errors.elements();
            while (e.hasMoreElements()) {
                errorsBuffer.append("\n");
                errorsBuffer.append(e.nextElement());
            }
            validationError = errorsBuffer.toString();
            return false;
        }
    }

    /**
     * Populates the new {@link NewEditMeteorogramInfoFormData} instance with
     * New/Edit Location screen data.
     * @return the {@link NewEditMeteorogramInfoFormData} with screen data.
     */
    private NewEditMeteorogramInfoFormData getNewEditFormData() {
        NewEditMeteorogramInfoFormData data = new NewEditMeteorogramInfoFormData();
        data.setName(getNameTextField().getString());
        data.setX(getXTextField().getString());
        data.setY(getYTextField().getString());
        data.setType(getModelChoiceGroup().getSelectedIndex());
        return data;
    }

    /**
     * Finalizes the Delete action in case the confirmation has been gathered.
     * It deletes the {@value #processedInfo} via the {@value #broker}.
     */
    protected void handleDeleteAction() {
        if (processedInfo == null) {
//#mdebug
            log.fatal("The info is null when trying to delete it!");
//#enddebug
            throw new NullPointerException("Internal state broken when deleting entry!");
        }
        broker.deleteMeteorogramInfo(processedInfo);
    }

    /**
     * Finalizes the New Location screen. It populates the {@value #processedInfo}
     * with data then stores it in {@value #broker}.
     */
    protected void handleNewAction() {
        if (processedInfo == null) {
//#mdebug
            log.fatal("The info is null when trying to create it!");
//#enddebug
            throw new NullPointerException("Internal state broken when creating entry!");
        }
        broker.createMeteorogramInfo(processedInfo);
    }

    /**
     * Finalizes the Edit Location screen. It populates the {@value #processedInfo}
     * with data then stores it in {@value #broker}.
     */
    protected void handleEditAction() {
        if (processedInfo == null) {
//#mdebug
            log.fatal("The info is null when trying to update it!");
//#enddebug
            throw new NullPointerException("Internal state broken when updating entry!");
        }
        broker.updateMeteorogramInfo(processedInfo);
    }

    /**
     * Populates {@value #processedInfo} with New/Edit Location screen data.
     */
    private void populateProcessedInfo(NewEditMeteorogramInfoFormData data) {
        processedInfo.setName(data.getName());
        processedInfo.setX(Integer.parseInt(data.getX()));
        processedInfo.setY(Integer.parseInt(data.getY()));
        MeteorogramType type = MeteorogramType.getByValue(data.getType());
        processedInfo.setType(type);
    }

    /**
     * Determines weather the {@value #downloadWaitScreen} is visible or not.
     * @return {@code true} if it {@value #downloadWaitScreen} is visible,
     *         {@code false} otherwise.
     */
    private boolean isDownloadWaitScreenVisible() {
        Display display = getDisplay();
        Displayable currentDisplayable = display.getCurrent();
        if (downloadWaitScreen != null
                && currentDisplayable == downloadWaitScreen) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public Display getDisplay() {
        return Display.getDisplay(this);
    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        switchDisplayable(null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Called when MIDlet is started.
     * Checks whether the MIDlet have been already started and initialize/starts
     * or resumes the MIDlet.
     */
    public void startApp() {
        if (midletPaused) {
            resumeMIDlet();
        } else {
            initialize();
            startMIDlet();
        }
        midletPaused = false;
    }

    /**
     * Called when MIDlet is paused.
     */
    public void pauseApp() {
        midletPaused = true;
    }

    /**
     * Called to signal the MIDlet to terminate.
     * @param unconditional if true, then the MIDlet has to be unconditionally
     *                      terminated and all resources has to be released.
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Removes all entries from {@value #mainList}, then populates it with
     * {@code newMeteorogramInfos}.
     * @param newMeteorogramInfos the {@link Vector} with {@link MeteorogramInfo}s
     *                            to populate {@value #mainList}
     */
    public void readMeteorogramInfo(Vector newMeteorogramInfos) {
        if (newMeteorogramInfos != null) {
//#mdebug
            log.info("Populating main list with new meteorogram data");
//#enddebug
            mainList.deleteAll();
            // Let's remove info related commands for a while
            mainList.removeCommand(getShowCommand());
            mainList.removeCommand(getEditCommand());
            mainList.removeCommand(getDeleteCommand());
            infoToMainListIndex = new Hashtable(newMeteorogramInfos.size());
            Enumeration infos = newMeteorogramInfos.elements();
            while (infos.hasMoreElements()) {
                MeteorogramInfo info = (MeteorogramInfo) infos.nextElement();
                Image image = null;
                switch (info.dataAvailability().getValue()) {
                    case Availability.NOT_AVAILABLE_VALUE:
                        image = getForecastNotAvailableImage();
                        break;
                    case Availability.AVAILABLE_VALUE:
                        image = getForecastAvailableImage();
                        break;
                    case Availability.AVAILABLE_OLD_VALUE:
                        image = getForecastAvailableOldImage();
                        break;
                    default:
//#mdebug
                        log.error("Cannot determine info's data availability: " + info);
//#enddebug
                }
                int position = mainList.append(info.getName(), image);
                infoToMainListIndex.put(info, new Integer(position));
            }
            // Restore info related commands if there was an info added.
            if (!infoToMainListIndex.isEmpty()) {
                mainList.addCommand(getShowCommand());
                mainList.addCommand(getEditCommand());
                mainList.addCommand(getDeleteCommand());
            }
        } else {
//#mdebug
            log.error("Trying to populate list with null!");
//#enddebug
        }
    }

    /**
     * Appends {@code addedMeteorogramInfo} to the end of the {@value #mainList}.
     * @param addedMeteorogramInfo the {@link MeteorogramInfo} to be added.
     */
    public void addedMeteorogramInfo(MeteorogramInfo addedMeteorogramInfo) {
        if (addedMeteorogramInfo != null
                && !infoToMainListIndex.containsKey(addedMeteorogramInfo)) {
//#mdebug
            log.info("Adding info to the list: " + addedMeteorogramInfo);
//#enddebug
            // If now the list is empty then we'll need to add info related
            // commands (check before adding, so in case of parallel execution
            // at least one of threads perfomrms adding).
            boolean addCommands = infoToMainListIndex.isEmpty();
            Image image = null;
            switch (addedMeteorogramInfo.dataAvailability().getValue()) {
                case Availability.NOT_AVAILABLE_VALUE:
                    image = getForecastNotAvailableImage();
                    break;
                case Availability.AVAILABLE_VALUE:
                    image = getForecastAvailableImage();
                    break;
                case Availability.AVAILABLE_OLD_VALUE:
                    image = getForecastAvailableOldImage();
                    break;
                default:
//#mdebug
                    log.error("Cannot determine info's data availability: " + addCommands);
//#enddebug
                }
            int position = mainList.append(addedMeteorogramInfo.getName(), image);
            infoToMainListIndex.put(addedMeteorogramInfo, new Integer(position));
            // If this is the first meteorogram added, let's add info related commands.
            if (infoToMainListIndex.size() == 1) {
                mainList.addCommand(getShowCommand());
                mainList.addCommand(getEditCommand());
                mainList.addCommand(getDeleteCommand());
            }
        } else {
//#mdebug
            log.error("Trying to add a null to the list or info already exists in list! "
                    + addedMeteorogramInfo);
//#enddebug
        }
    }

    /**
     * Deletes the {@code deletedMeteorogramInfo} from the {@value #mainList}.
     * @param deletedMeteorogramInfo the {@link MeteorogramInfo} to be deleted.
     */
    public void deletedMeteorogramInfo(MeteorogramInfo deletedMeteorogramInfo) {
        if (deletedMeteorogramInfo != null
                && infoToMainListIndex.containsKey(deletedMeteorogramInfo)) {
//#mdebug
            log.info("Deleting info from list: " + deletedMeteorogramInfo);
//#enddebug
            int deletedIndex =
                    ((Integer) infoToMainListIndex.remove(deletedMeteorogramInfo)).intValue();
            mainList.delete(deletedIndex);
            Enumeration infos = infoToMainListIndex.keys();
            while (infos.hasMoreElements()) {
                Object info = infos.nextElement();
                int index = ((Integer) infoToMainListIndex.get(info)).intValue();
                if (index > deletedIndex) {
                    infoToMainListIndex.put(info, new Integer(--index));
                }
            }
            // If this was the last element remove info related commands.
            if (infoToMainListIndex.isEmpty()) {
                mainList.removeCommand(getShowCommand());
                mainList.removeCommand(getEditCommand());
                mainList.removeCommand(getDeleteCommand());
            }
        } else {
//#mdebug
            log.error("Trying to delete null from list or info doesn't exist in list! "
                    + deletedMeteorogramInfo);
//#enddebug
        }
    }

    /**
     * Updates the {@code updatedMeteorogramInfo} in the {@value #mainList}.
     * @param updatedMeteorogramInfo the {@link MeteorogramInfo} to be updated.
     */
    public void updatedMeteorogramInfo(MeteorogramInfo updatedMeteorogramInfo) {
        if (updatedMeteorogramInfo != null
                && infoToMainListIndex.containsKey(updatedMeteorogramInfo)) {
//#mdebug
            log.info("Updating info in list: " + updatedMeteorogramInfo);
//#enddebug
            int index =
                    ((Integer) infoToMainListIndex.get(updatedMeteorogramInfo)).intValue();
            String currentName = mainList.getString(index);
            Image currentImage = mainList.getImage(index);
            String newName = updatedMeteorogramInfo.getName();
            Image newImage = null;
            switch (updatedMeteorogramInfo.dataAvailability().getValue()) {
                case Availability.NOT_AVAILABLE_VALUE:
                    newImage = getForecastNotAvailableImage();
                    break;
                case Availability.AVAILABLE_VALUE:
                    newImage = getForecastAvailableImage();
                    break;
                case Availability.AVAILABLE_OLD_VALUE:
                    newImage = getForecastAvailableOldImage();
                    break;
                default:
//#mdebug
                    log.error("Cannot determine info's data availability: " + updatedMeteorogramInfo);
//#enddebug
                }
            if (!currentName.equals(newName) || currentImage != newImage) {
                mainList.set(index, newName, newImage);
            }

        } else {
//#mdebug
            log.error("Trying to update null in list or info that doesn't exist in list! "
                    + updatedMeteorogramInfo);
//#enddebug
        }
    }

    /**
     * Set's the correct test in {@value #downloadWaitScreen} based on the
     * {@code status} received.
     * @param source the {@link StatusReporter} that triggered this event.
     * @param status the {@link Status} to generate the text.
     */
    public void statusUpdate(StatusReporter source, Status status) {
        if (source == null || status == null) {
//#mdebug
            log.error("Status or source is null: status = " + status
                    + "source = " + source);
//#enddebug
            throw new NullPointerException("Updating status with nulls");
        }
        if (status == Status.FINISHED || status == Status.CANCELLED) {
            source.removeListener(this);
        }
        if (isDownloadWaitScreenVisible()) {
//#mdebug
            log.trace("Setting progress to: " + status.getProgress());
//#enddebug
            StringBuffer buffer = new StringBuffer("Downloading... ");
            buffer.append("(").append(status.getProgress()).append("% done)");
            downloadWaitScreen.setText(buffer.toString());
        } else {
//#mdebug
            log.warn("The download waiting screen is not visible status update has been "
                    + "recevied from: " + source);
//#enddebug
        }
    }

    /**
     * Removes this instance from the download task and optionally cancels the task.
     * @param cancelTask weather the task shall be canceled.
     */
    private void unregisterAtDownloadTask(boolean cancelTask) {
        ForecastDataDownloader task =
                (ForecastDataDownloader) getDownloadWaitScreen().getTask();
        if (task != null) {
            task.removeListener(this);
            if (cancelTask) {
//#mdebug
                log.debug("Cancelling the task");
//#enddebug
                task.cancel();
            }
        }
    }
}
