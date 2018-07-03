/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.dialog;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.AnchorPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.TilePaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class MessageDialog extends StackPane {

	/**
	 * Enum to represent user selection on the dialog whether YES/OK, NO or
	 * Cancel
	 */
	public static enum Answer {

		/**
		 * Used to represent that the user has selected the option corresponding
		 * with YES or OK. Sometimes it is represented by YES button sometimes OK
		 * button.
		 */
		YES_OK,
		/**
		 * Used to represent that the user has selected the option corresponding
		 * with NO.
		 */
		NO,
		/**
		 * Used to represent that the user has selected the option corresponding
		 * with CANCEL.
		 */
		CANCEL
	}

	/**
	 * The number of buttons to be displayed in the dialog
	 */
	public static enum ButtonType {

		/**
		 * Only one button is shown. The default label is OK
		 */
		OK,
		/**
		 * Two buttons are shown, the default labels are 'Yes' and 'No'. Default
		 * {@link Answer} is {@link Answer#YES_OK} and the escape answer is
		 * {@link Answer#NO}
		 */
		YES_NO,
		/**
		 * Three buttons are shown, the default labels are 'Yes', 'No' and
		 * 'Cancel'. Developer are encouraged to change these label by calling {@link MessageDialog#setYesOkButtonText(String)}
		 * , {@link MessageDialog#setNoButtonText(String)}, and
		 * {@link MessageDialog#setCancelButtonText(String)}. Default
		 * {@link Answer} is {@link Answer#YES_OK} and the escape answer is
		 * {@link Answer#CANCEL}
		 */
		YES_NO_CANCEL
	}

	public static enum DialogType {

		/**
		 * Confirmation dialog. By default the buttons are
		 * {@link ButtonType#YES_NO}
		 *
		 * @see
		 * MessageDialog#setButtonType(com.panemu.tiwulfx.dialog.MessageDialog.ButtonType)
		 */
		CONFIRM(ButtonType.YES_NO),
		/**
		 * Error dialog. By default the buttons are {@link ButtonType#OK}
		 *
		 * @see
		 * MessageDialog#setButtonType(com.panemu.tiwulfx.dialog.MessageDialog.ButtonType)
		 */
		ERROR(ButtonType.OK),
		/**
		 * Information dialog. By default the buttons are {@link ButtonType#OK}
		 *
		 * @see
		 * MessageDialog#setButtonType(com.panemu.tiwulfx.dialog.MessageDialog.ButtonType)
		 */
		INFO(ButtonType.OK),
		/**
		 * Warning dialog. By default the buttons are {@link ButtonType#OK}
		 *
		 * @see
		 * MessageDialog#setButtonType(com.panemu.tiwulfx.dialog.MessageDialog.ButtonType)
		 */
		WARNING(ButtonType.OK);
		private final ButtonType type;

		DialogType(ButtonType type) {
			this.type = type;
		}

		public ButtonType getDefaultButtonType() {
			return type;
		}
	}
	private Button btnYes = new Button();
	private Button btnNo = new Button();
	private Button btnCancel = new Button();
	private ObjectProperty<ButtonType> buttonType = new SimpleObjectProperty<>();
	private ObjectProperty<Answer> defaultAnswer = new SimpleObjectProperty<>(Answer.YES_OK);
	private ObjectProperty<Answer> escapeAnswer = new SimpleObjectProperty<>();
	private StringProperty yesText = new SimpleStringProperty(TiwulFXUtil.getLiteral("ok"));
	private StringProperty noText = new SimpleStringProperty(TiwulFXUtil.getLiteral("no"));
	private StringProperty cancelText = new SimpleStringProperty(TiwulFXUtil.getLiteral("cancel"));
	protected Label lblTitle;
	protected Label lblMessage;
	protected TilePane pnlButton;
	protected ImageView imageView;
	private double dragOffsetX = 0;
	private double dragOffsetY = 0;
	private double initialWidth;
	private double initialHeight;
	private Point2D dragAnchor;
	private VBox pnlClient;

	public MessageDialog(Throwable throwable) {
		this(DialogType.ERROR, throwable);

	}

	public MessageDialog(DialogType dialogType) {
		this(dialogType, null);
	}

	private MessageDialog(DialogType dialogType, Throwable throwable) {

		getStyleClass().addAll("dialog");
//        setEffect(new DropShadow());
		lblTitle = LabelBuilder.create().styleClass("title").build();

		HBox pnlTitle = HBoxBuilder.create()
				  .children(lblTitle)
				  .styleClass("header")
				  .build();

		pnlTitle.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				dragOffsetX = mouseEvent.getSceneX();
				dragOffsetY = mouseEvent.getSceneY();
			}
		});
		pnlTitle.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (getCursor().equals(Cursor.DEFAULT)) {
					stage.setX(mouseEvent.getScreenX() - dragOffsetX);
					stage.setY(mouseEvent.getScreenY() - dragOffsetY);
				}
			}
		});

		lblMessage = LabelBuilder.create().wrapText(true).build();
		VBox pnlMessage = VBoxBuilder.create()
				  .children(lblMessage)
				  .styleClass("message-panel")
				  .spacing(10)
				  .build();
		VBox.setVgrow(lblMessage, Priority.NEVER);
		VBox.setVgrow(pnlMessage, Priority.ALWAYS);

		if (throwable != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			TextArea text = new TextArea(sw.toString());
			text.setEditable(false);
			text.setWrapText(true);
			text.setPrefWidth(60 * 8);
			text.setPrefHeight(20 * 12);
			VBox.setVgrow(text, Priority.ALWAYS);
			pnlMessage.getChildren().add(text);
		}

		btnCancel.setMaxWidth(Double.MAX_VALUE);
		btnNo.setMaxWidth(Double.MAX_VALUE);
		btnYes.setMaxWidth(Double.MAX_VALUE);

		pnlButton = TilePaneBuilder.create()
				  .styleClass("button-panel")
				  .hgap(10)
				  .build();

		pnlClient = VBoxBuilder.create()
				  .children(pnlTitle, pnlMessage, pnlButton)
				  .styleClass("dialog-root")
				  .build();

		imageView = ImageViewBuilder.create().build();
		AnchorPane.setTopAnchor(imageView, 10d);
		AnchorPane.setRightAnchor(imageView, 10d);
		AnchorPane.setLeftAnchor(imageView, 10d);
		AnchorPane pnlImage = AnchorPaneBuilder.create().children(imageView).mouseTransparent(true).build();

		getChildren().addAll(pnlClient, pnlImage);

		setMaxWidth(1000);

		this.buttonType.addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable o) {
				setDefaultButton();
				if (buttonType.get() == ButtonType.OK) {
					pnlButton.setPrefColumns(1);
				} else if (buttonType.get() == ButtonType.YES_NO) {
					pnlButton.setPrefColumns(2);
				} else {
					pnlButton.setPrefColumns(3);
				}
			}
		});
		this.buttonType.set(dialogType.getDefaultButtonType());
		Image image = null;
		switch (dialogType) {
			case CONFIRM:
				image = TiwulFXUtil.getGraphicFactory().getDialogConfirmImage();
				yesText.set(TiwulFXUtil.getLiteral("yes"));
				setTitle(TiwulFXUtil.getLiteral("confirmation"));
				break;
			case ERROR:
				image = TiwulFXUtil.getGraphicFactory().getDialogErrorImage();
				setTitle(TiwulFXUtil.getLiteral("error"));
				break;
			case INFO:
				image = TiwulFXUtil.getGraphicFactory().getDialogInfoImage();
				setTitle(TiwulFXUtil.getLiteral("information"));
				break;
			case WARNING:
				image = TiwulFXUtil.getGraphicFactory().getDialogWarningImage();
				setTitle(TiwulFXUtil.getLiteral("warning"));
				break;
		}
		imageView.setImage(image);

		initButtonBehaviors();

		initResizeRoutine();

	}

	/**
	 * Change default image
	 *
	 * @param image
	 */
	public void setImage(Image image) {
		imageView.setImage(image);
	}

	/**
	 * Set the title of dialog
	 *
	 * @param title
	 */
	public void setTitle(String title) {
		lblTitle.setText(title);
	}

	public String getTitle() {
		return lblTitle.getText();
	}

	/**
	 * Set the message displayed in the dialog
	 *
	 * @param message
	 */
	public void setMessage(String message) {
		lblMessage.setText(message);
	}

	public String getMessage() {
		return lblMessage.getText();
	}

	private void initButtonBehaviors() {
		btnCancel.textProperty().bind(cancelText);
		btnNo.textProperty().bind(noText);
		btnYes.textProperty().bind(yesText);

		btnCancel.setMinWidth(USE_PREF_SIZE);
		btnNo.setMinWidth(USE_PREF_SIZE);
		btnYes.setMinWidth(USE_PREF_SIZE);

		btnCancel.cancelButtonProperty().bind(escapeAnswer.isEqualTo(Answer.CANCEL));
		btnNo.cancelButtonProperty().bind(escapeAnswer.isEqualTo(Answer.NO));
		btnYes.cancelButtonProperty().bind(escapeAnswer.isEqualTo(Answer.YES_OK));

		btnCancel.defaultButtonProperty().bind(defaultAnswer.isEqualTo(Answer.CANCEL));
		btnNo.defaultButtonProperty().bind(defaultAnswer.isEqualTo(Answer.NO));
		btnYes.defaultButtonProperty().bind(defaultAnswer.isEqualTo(Answer.YES_OK));

		btnCancel.setOnAction(buttonClickHandler);
		btnNo.setOnAction(buttonClickHandler);
		btnYes.setOnAction(buttonClickHandler);
	}

	private void initResizeRoutine() {
		setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				Insets padding = pnlClient.getInsets();
				double dragAreaX = stage.getWidth() - 5 - padding.getRight();
				double dragAreaY = stage.getHeight() - 5 - padding.getBottom();
				if (mouseEvent.getSceneX() > dragAreaX && mouseEvent.getSceneY() > dragAreaY) {
					setCursor(Cursor.SE_RESIZE);
				} else if (mouseEvent.getSceneX() > dragAreaX) {
					setCursor(Cursor.E_RESIZE);
				} else if (mouseEvent.getSceneY() > dragAreaY) {
					setCursor(Cursor.S_RESIZE);
				} else {
					setCursor(Cursor.DEFAULT);
				}
			}
		});

		setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				initialWidth = stage.getWidth();
				initialHeight = stage.getHeight();
				dragAnchor = new Point2D(t.getSceneX(), t.getSceneY());
			}
		});

		setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				if (getCursor().equals(Cursor.DEFAULT)) {
					return;
				}
				double locX = t.getSceneX();
				double locY = t.getSceneY();
				if (getCursor().equals(Cursor.S_RESIZE) || getCursor().equals(Cursor.SE_RESIZE)) {
					stage.setHeight(initialHeight + locY - dragAnchor.getY());
				}
				if (getCursor().equals(Cursor.E_RESIZE) || getCursor().equals(Cursor.SE_RESIZE)) {
					stage.setWidth(initialWidth + locX - dragAnchor.getX());
				}
			}
		});
	}

	/**
	 * Set the buttons to be displayed.
	 *
	 * @param type
	 * @see Answer
	 */
	public void setButtonType(ButtonType type) {
		buttonType.set(type);
	}

	private void setDefaultButton() {
		pnlButton.getChildren().clear();
		switch (buttonType.get()) {
			case OK:
				pnlButton.getChildren().add(btnYes);
				defaultAnswer.set(Answer.YES_OK);
				escapeAnswer.set(Answer.YES_OK);
				break;
			case YES_NO:
				pnlButton.getChildren().add(btnYes);
				pnlButton.getChildren().add(btnNo);
				defaultAnswer.set(Answer.YES_OK);
				escapeAnswer.set(Answer.NO);
				break;
			case YES_NO_CANCEL:
				pnlButton.getChildren().add(btnYes);
				pnlButton.getChildren().add(btnNo);
				pnlButton.getChildren().add(btnCancel);
				defaultAnswer.set(Answer.YES_OK);
				escapeAnswer.set(Answer.CANCEL);
				break;
		}
	}

	/**
	 * Change YES/OK button's text
	 *
	 * @param text
	 */
	public void setYesOkButtonText(String text) {
		yesText.set(text);
	}

	/**
	 * Set text for NO button
	 *
	 * @param text
	 */
	public void setNoButtonText(String text) {
		noText.set(text);
	}

	/**
	 * Set text for Cancel button. It is relevant only for
	 * {@link ButtonType#YES_NO_CANCEL}
	 *
	 * @param text
	 */
	public void setCancelButtonText(String text) {
		cancelText.set(text);
	}

	/**
	 * Set the default answer. The corresponding button will be focused by
	 * default so if user press enter it is just like he clicks it.
	 *
	 * @param answer
	 */
	public void setDefaultAnswer(Answer answer) {
		defaultAnswer.set(answer);
	}

	/**
	 * Set the answer if user press Escape on keyboard
	 *
	 * @param answer
	 */
	public void setEscapeAnswer(Answer answer) {
		escapeAnswer.set(answer);
		btnCancel.setOnAction(buttonClickHandler);
	}
	private Stage stage;

	/**
	 * Show the dialog. It will wait for user's answer by clicking a button,
	 * pressing enter or pressing escape
	 *
	 * @param owner nullable. If not null, the message dialog will be displayed
	 * on top center of owner, otherwise on the center of main screen.
	 * @return user's answer
	 */
	public Answer show(final Window owner) {
		stage = new Stage(StageStyle.TRANSPARENT);

		stage.setScene(new Scene(this));
		if (owner instanceof Stage) {
			stage.initOwner(owner);
		}
		stage.initModality(Modality.WINDOW_MODAL);
		TiwulFXUtil.setTiwulFXStyleSheet(stage.getScene());
		stage.getScene().setFill(Color.TRANSPARENT);
		stage.setOnShown(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				Button btn;
				if (btnCancel.isDefaultButton()) {
					btn = btnCancel;
				} else if (btnNo.isDefaultButton()) {
					btn = btnNo;
				} else {
					btn = btnYes;
				}
				btn.requestFocus();
			}
		});

		stage.showAndWait();
		return answer;
	}
	private Answer answer;
	private EventHandler<ActionEvent> buttonClickHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent t) {
			Object source = t.getSource();

			if (source.equals(btnCancel)) {
				answer = Answer.CANCEL;
			} else if (source.equals(btnNo)) {
				answer = Answer.NO;
			} else {
				answer = Answer.YES_OK;
			}

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					FadeTransition fadeTransition = new FadeTransition(Duration.millis(TiwulFXUtil.DURATION_FADE_ANIMATION), MessageDialog.this);

					fadeTransition.setFromValue(1.0);
					fadeTransition.setToValue(0.0);
					fadeTransition.setOnFinished(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							stage.hide();
						}
					});
					fadeTransition.play();
				}
			});
		}
	};
}
