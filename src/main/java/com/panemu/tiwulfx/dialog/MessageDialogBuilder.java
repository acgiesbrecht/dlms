/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.dialog;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.dialog.MessageDialog.Answer;
import com.panemu.tiwulfx.dialog.MessageDialog.ButtonType;
import com.panemu.tiwulfx.dialog.MessageDialog.DialogType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * This class provide a convenient way to create a dialog, display it and get
 * user's answer. Developer is encouraged to change the button's default label
 * by calling {@link #yesOkButtonText(String)}, {@link #noButtonText(String)}
 * and {@link #cancelButtonText(String)}.
 * <pre>
 * {@code 
 *     MessageDialog.Answer answer = MessageDialogBuilder.confirmation()
 *               .title("Delete Confirmation")
 *               .message("Are you sure to delete selected record?")
 *               .yesOkButtonText("Delete")
 *               .noButtonText("Don't Delete")
 *               .buttonType(MessageDialog.ButtonType.YES_NO_CANCEL)
 *               .defaultAnswer(MessageDialog.Answer.CANCEL)
 *               .escapeAnswer(MessageDialog.Answer.NO)
 *               .show(null);
 *       if (answer == MessageDialog.Answer.YES_OK) {
 *           System.out.println("do something");
 *       }
 * 
 * }
 * </pre>
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class MessageDialogBuilder {

    private MessageDialog dialog;

    private MessageDialogBuilder(DialogType dialogType) {
        dialog = new MessageDialog(dialogType);
    }
    
    private MessageDialogBuilder(Throwable throwable) {
        dialog = new MessageDialog(throwable);
    }

    /**
     * Create {@link DialogType#INFO} dialog
     *
     * @return
     */
    public static MessageDialogBuilder info() {
        return new MessageDialogBuilder(DialogType.INFO);
    }

    /**
     * Create {@link DialogType#WARNING} dialog
     *
     * @return
     */
    public static MessageDialogBuilder warning() {
        return new MessageDialogBuilder(DialogType.WARNING);
    }

    /**
     * Create {@link DialogType#ERROR} dialog
     *
     * @return
     */
    public static MessageDialogBuilder error() {
        return new MessageDialogBuilder(DialogType.ERROR);
    }
    
    /**
     * Create {@link DialogType#ERROR} dialog
     *
     * @return
     */
    public static MessageDialogBuilder error(Throwable throwable) {
        return new MessageDialogBuilder(throwable);
    }

    /**
     * Create {@link DialogType#CONFIRM} dialog
     *
     * @return
     */
    public static MessageDialogBuilder confirmation() {
        return new MessageDialogBuilder(DialogType.CONFIRM);
    }

    /**
     * Build and return the dialog
     *
     * @return
     */
    public MessageDialog build() {
        return dialog;
    }

    /**
     * It will be translated using {@link TiwulFXUtil#getLiteral(java.lang.String)}.
     * @see MessageDialog#setYesOkButtonText(java.lang.String)
     * @param text
     * @return
     */
    public MessageDialogBuilder yesOkButtonText(String text) {
        dialog.setYesOkButtonText(TiwulFXUtil.getLiteral(text));
        return this;
    }

    /**
     * It will be translated using {@link TiwulFXUtil#getLiteral(java.lang.String)}.
     * @see MessageDialog#setNoButtonText(java.lang.String)
     * @param text
     * @return
     */
    public MessageDialogBuilder noButtonText(String text) {
        dialog.setNoButtonText(TiwulFXUtil.getLiteral(text));
        return this;
    }

    /**
     * It will be translated using {@link TiwulFXUtil#getLiteral(java.lang.String)}.
     * @see MessageDialog#setCancelButtonText(java.lang.String)
     * @param text
     * @return
     */
    public MessageDialogBuilder cancelButtonText(String text) {
        dialog.setCancelButtonText(TiwulFXUtil.getLiteral(text));
        return this;
    }

    /**
     * @see
     * MessageDialog#setDefaultAnswer(com.panemu.tiwulfx.dialog.MessageDialog.Answer)
     * @param answer
     * @return
     */
    public MessageDialogBuilder defaultAnswer(Answer answer) {
        dialog.setDefaultAnswer(answer);
        return this;
    }

    /**
     * @see
     * MessageDialog#setEscapeAnswer(com.panemu.tiwulfx.dialog.MessageDialog.Answer)
     * @param answer
     * @return
     */
    public MessageDialogBuilder escapeAnswer(Answer answer) {
        dialog.setEscapeAnswer(answer);
        return this;
    }

    /**
     * @see
     * MessageDialog#setButtonType(com.panemu.tiwulfx.dialog.MessageDialog.ButtonType)
     * @param buttonType
     * @return
     */
    public MessageDialogBuilder buttonType(ButtonType buttonType) {
        dialog.setButtonType(buttonType);
        return this;
    }

    /**
     * @see MessageDialog#show(javafx.stage.Window)
     * @param owner nullable. If not null, the message dialog will be displayed on top center of owner, otherwise on the
	 * center of main screen.
     * @return
     */
    public MessageDialog.Answer show(Window owner) {
        return dialog.show(owner);
    }

    /**
     * It will be translated using {@link TiwulFXUtil#getLiteral(java.lang.String)}.
     * @see MessageDialog#setTitle(java.lang.String)
     * @param text
     * @return
     */
    public MessageDialogBuilder title(String text) {
        dialog.setTitle(TiwulFXUtil.getLiteral(text));
        return this;
    }

    /**
     * Set the message text. It will be translated using {@link TiwulFXUtil#getLiteral(java.lang.String)}.
     * If the literal is not found, the message will be displayed as is
     * @see MessageDialog#setMessage(java.lang.String)
     * @param message
     * @return
     */
    public MessageDialogBuilder message(String message) {
        dialog.setMessage(TiwulFXUtil.getLiteral(message));
        return this;
    }
    
    /**
     * Set the message text. It will be translated using {@link TiwulFXUtil#getLiteral(java.lang.String, java.lang.Object[]) }
     * @see MessageDialog#setMessage(java.lang.String)
     * @param message
     * @param params parameter for translation
     * @return
     */
    public MessageDialogBuilder message(String message, Object... params) {
        dialog.setMessage(TiwulFXUtil.getLiteral(message, params));
        return this;
    }

    /**
     * @see MessageDialog#setImage(javafx.scene.image.Image)
     * @param image
     * @return
     */
    public MessageDialogBuilder image(Image image) {
        dialog.setImage(image);
        return this;
    }
}
