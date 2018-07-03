/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control.skin;

import com.panemu.tiwulfx.common.TableCriteria;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.control.LookupField;
import com.panemu.tiwulfx.control.behavior.LookupFieldBehavior;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LookupFieldSkin<T> extends BehaviorSkinBase<LookupField<T>, LookupFieldBehavior<T>> {

	private static final String PROP_SHOWING_SUGGESTION = "SHOWING_SUGGESTION";
	private static final String PROP_SHOWING_LOOKUP_WINDOW = "SHOWING_LOOKUP_WINDOW";
	private static final String PROP_RESETTING_DISPLAY_TEXT = "RESETTING_DISPLAY_TEXT";
	private TextField textField;
	private Button button;
	private PopupControl popup;
	private boolean detectTextChanged = true;
	private Timer waitTimer;
	private LoaderTimerTask loaderTimerTask;
	private LookupField<T> lookupField;
	/**
	 * flag to
	 */
	public boolean needValidation = true;

	public LookupFieldSkin(LookupField<T> control) {
		super(control, new LookupFieldBehavior<>(control));
		this.lookupField = control;
		// move focus in to the textfield
		lookupField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
				if (hasFocus) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							textField.requestFocus();
						}
					});
				}

			}
		});
		initialize();

		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
				if (!hasFocus) {
					validate();
				}
			}
		});

		lookupField.addEventFilter(InputEvent.ANY, new EventHandler<InputEvent>() {
			@Override
			public void handle(InputEvent t) {
				if (textField == null) {
					return;
				}

                // When the user hits the enter or F4 keys, we respond before 
				// ever giving the event to the TextField.
				if (t instanceof KeyEvent) {
					KeyEvent ke = (KeyEvent) t;

					if ((ke.getCode() == KeyCode.F10 || ke.getCode() == KeyCode.ESCAPE || ke.getCode() == KeyCode.ENTER)
							  && !ke.isControlDown()) {

                        // RT-23275: The TextField fires F10 and ESCAPE key events
						// up to the parent, which are then fired back at the 
						// TextField, and this ends up in an infinite loop until
						// the stack overflows. So, here we consume these two
						// events and stop them from going any further.
						t.consume();
						return;
					}
				}
			}
		});

		textField.promptTextProperty().bind(lookupField.promptTextProperty());
		getSkinnable().requestLayout();

		registerChangeListener(control.showingSuggestionProperty(), PROP_SHOWING_SUGGESTION);
		registerChangeListener(control.showingLookupDialogProperty(), PROP_SHOWING_LOOKUP_WINDOW);
		registerChangeListener(control.resettingDisplayTextProperty(), PROP_RESETTING_DISPLAY_TEXT);
	}

	@Override
	protected void handleControlPropertyChanged(String string) {
		super.handleControlPropertyChanged(string);
		if (PROP_SHOWING_SUGGESTION.equals(string)) {
			if (lookupField.isShowingSuggestion()) {
				showSuggestion();
			} else {
				getPopup().hide();
			}
		} else if (PROP_SHOWING_LOOKUP_WINDOW.equals(string)) {
			if (lookupField.isShowingLookupDialog()) {
				showLookupDialog(false);
			}
		} else if (PROP_RESETTING_DISPLAY_TEXT.equals(string)) {
			if (lookupField.isResettingDisplayText()) {
				updateTextField();
			}
		}
	}

	private PopupControl getPopup() {
		if (popup == null) {
			createPopup();
		}
		return popup;
	}

	private double getListViewPrefHeight() {
		double ph;
		double ch = listView.getItems().size() * 25;
		ph = Math.min(ch, 200);

		return ph;
	}

	private void createPopup() {
		popup = new PopupControl() {
			{
				setSkin(new Skin() {
					@Override
					public Skinnable getSkinnable() {
						return LookupFieldSkin.this.lookupField;
					}

					@Override
					public Node getNode() {
						return listView;
					}

					@Override
					public void dispose() {
					}
				});
			}
		};
		popup.setAutoHide(true);
		popup.setAutoFix(true);
		popup.setHideOnEscape(true);
		popup.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				popup.hide();
			}
		});

		listView.setCellFactory(new Callback() {
			@Override
			public Object call(Object p) {
				return new PropertyListCell(lookupField.getPropertyName());
			}
		});

		/**
		 * Taken from
		 * {@link com.sun.javafx.scene.control.skin.ComboBoxListViewSkin}
		 */
		listView.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
                // RT-18672: Without checking if the user is clicking in the 
				// scrollbar area of the ListView, the comboBox will hide. Therefore,
				// we add the check below to prevent this from happening.
				EventTarget target = t.getTarget();
				if (target instanceof Parent) {
					List<String> s = ((Parent) target).getStyleClass();
					if (s.contains("thumb")
							  || s.contains("track")
							  || s.contains("decrement-arrow")
							  || s.contains("increment-arrow")) {
						return;
					}
				}
				needValidation = false;
				lookupField.setValue(listView.getSelectionModel().getSelectedItem());
				popup.hide();
			}
		});

		listView.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					needValidation = false;
					lookupField.setValue(listView.getSelectionModel().getSelectedItem());
					popup.hide();
				} else if (t.getCode() == KeyCode.ESCAPE) {
					popup.hide();
				}
			}
		});

	}

	private void initialize() {
		textField = new TextField();
		textField.setFocusTraversable(true);
		textField.setEditable(!lookupField.getDisableManualInput());
		textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
				textField.selectEnd();
			}
		});

		button = new Button();
		button.setFocusTraversable(false);
		button.setGraphic(TiwulFXUtil.getGraphicFactory().createLookupGraphic());
		StackPane.setAlignment(textField, Pos.CENTER_LEFT);
		StackPane.setAlignment(button, Pos.CENTER_RIGHT);
		this.getChildren().addAll(textField, button);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				if (!lookupField.isFocused()) {
					/**
					 * Need to make this control become focused. Otherwise
					 * changing value in LookupColumn while the LookuField cell
					 * editor
					 * is not focused before, won't trigger commitEdit()
					 */
					lookupField.requestFocus();
				}
				getSkinnable().showLookupDialog();
			}
		});
		updateTextField();
		lookupField.valueProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue ov, Object t, Object t1) {
				updateTextField();
			}
		});

		lookupField.markInvalidProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (oldValue && !newValue && needValidation) {
					validate();
				}
			}
		});

		textField.textProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable o) {
				if (detectTextChanged) {
					if (waitTimer != null) {
						loaderTimerTask.setObsolete(true);
						waitTimer.cancel();
						waitTimer.purge();
					}

					if (textField.getText() == null || textField.getText().trim().isEmpty()) {
						lookupField.setValue(null);
						return;
					}
					lookupField.markInvalidProperty().set(true);
					needValidation = true;

					if (lookupField.getShowSuggestionWaitTime() >= 0) {
						waitTimer = new Timer("lookupTimer");
						loaderTimerTask = new LoaderTimerTask(waitTimer);
						waitTimer.schedule(loaderTimerTask, lookupField.getShowSuggestionWaitTime());
					}
				}
			}
		});

	}

	private void updateTextField() {
		detectTextChanged = false;
		needValidation = false;
		if (lookupField.getValue() == null) {
			textField.setText("");
			lookupField.markInvalidProperty().set(false);
			detectTextChanged = true;
			return;
		}
		try {
			Object value = PropertyUtils.getSimpleProperty(lookupField.getValue(), lookupField.getPropertyName());
			if (value != null) {
				textField.setText(value.toString());
			} else {
				textField.setText("");
			}
			lookupField.markInvalidProperty().set(false);
			detectTextChanged = true;
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Point2D getPrefPopupPosition() {
		Point2D p = getSkinnable().localToScene(0.0, 0.0);
		Point2D p2 = new Point2D(p.getX() + getSkinnable().getScene().getX() + getSkinnable().getScene().getWindow().getX(), p.getY() + getSkinnable().getScene().getY() + getSkinnable().getScene().getWindow().getY() + getSkinnable().getHeight());
		return p2;
	}

	private void positionAndShowPopup() {
		if (getPopup().getSkin() == null) {
			getSkinnable().getScene().getRoot().impl_processCSS(true);
		}

		Point2D p = getPrefPopupPosition();

		/**
		 * In LookupColumn, sometimes the lookupfield disappears due to commit
		 * editing before the popup appears. In this case,
		 * lookupField.getScene() will be null.
		 */
		Scene scene = lookupField.getScene();
		if (scene != null) {
			getPopup().show(scene.getWindow(), p.getX(), p.getY());
		}
	}

	@Override
	protected void layoutChildren(final double x, final double y, final double w, final double h) {

		double obw = button.prefWidth(-1);

		double displayWidth = getSkinnable().getWidth()
				  - (getSkinnable().getInsets().getLeft() + getSkinnable().getInsets().getRight() + obw);

		textField.resizeRelocate(x, y, w, h);
		button.resizeRelocate(x + displayWidth, y, obw, h);
	}

	static class PropertyListCell<T> extends ListCell<T> {

		private String propertyName;

		public PropertyListCell(String propertyName) {
			this.propertyName = propertyName;
		}

		@Override
		protected void updateItem(T t, boolean bln) {
			super.updateItem(t, bln);
			if (t != null) {
				try {
					Object value = PropertyUtils.getSimpleProperty(t, propertyName);
					if (value != null) {
						setText(value.toString());
					}
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	private void validate() {
		if (needValidation && !textField.getText().isEmpty()) {
			loaderTimerTask.setObsolete(true);
			List<T> data = lookupField.getController().loadDataForPopup(lookupField.getPropertyName(), textField.getText(), TableCriteria.Operator.eq);
			if (data.size() == 1) {
				lookupField.setValue(data.get(0));
			} else {
				showLookupDialog(true);
			}
		}
	}

	private void showLookupDialog(boolean filterIt) {
		needValidation = false;
		T newValue;
		if (filterIt) {
			newValue = lookupField.getController().show(lookupField.getScene().getWindow(), lookupField.getValue(), lookupField.getPropertyName(), textField.getText());
		} else {
			newValue = lookupField.getController().show(lookupField.getScene().getWindow(), lookupField.getValue(), lookupField.getPropertyName());
		}
		if (newValue == lookupField.getValue()) {
			updateTextField();
		} else {
			lookupField.setValue(newValue);
		}
	}

	/**
	 * Get the reference to the underlying textfield. This method is used by
	 * LookupTableCell.
	 * <p>
	 * @return TextField
	 */
	public TextField getTextField() {
		return textField;
	}

	private void showSuggestion() {
		List<T> data = lookupField.getController().loadDataForPopup(lookupField.getPropertyName(), textField.getText());
		listView.getItems().clear();
		listView.getItems().addAll(data);
		positionAndShowPopup();
	}

	private class LoaderTimerTask extends TimerTask {

		private boolean obsolete = false;
		private Timer timer;

		public LoaderTimerTask(Timer timer) {
			this.timer = timer;
		}

		public void setObsolete(boolean obsolete) {
			this.obsolete = obsolete;
		}

		@Override
		public void run() {
			if (!obsolete) {
				final List<T> data = lookupField.getController().loadDataForPopup(lookupField.getPropertyName(), textField.getText());
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!obsolete) {
							listView.getItems().clear();
							if (!data.isEmpty()) {
								listView.getItems().addAll(data);
								positionAndShowPopup();
							}
						}
					}
				});
			}
			timer.cancel();
			timer.purge();
		}
	}
	private ListView<T> listView = new ListView<T>() {
		@Override
		protected double computeMinHeight(double width) {
			return 30;
		}

		@Override
		protected double computePrefHeight(double width) {

			return getListViewPrefHeight();
		}
	};
}
