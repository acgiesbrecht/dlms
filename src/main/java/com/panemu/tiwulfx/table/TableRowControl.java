/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class TableRowControl<R> extends TableRow<R> {

	private boolean selected;
	private TableControl<R> tblView;
	private static final PseudoClass PSEUDO_CLASS_CHANGED = PseudoClass.getPseudoClass("changed");
	public TableRowControl(final TableControl tblView) {
		super();
		this.setAlignment(Pos.CENTER_LEFT);
		this.tblView = tblView;
		attachScrollListener();
		this.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().equals(MouseButton.PRIMARY)
						&& event.getClickCount() == 2
						&& TableRowControl.this.getIndex() < tblView.getRecords().size()) {
					tblView.getController().doubleClick(tblView.getSelectionModel().getSelectedItem());
				}
			}
		});

		tblView.modeProperty().addListener(new WeakChangeListener<>(modeChangeListener));
		tblView.getSelectionModel().selectedIndexProperty().addListener(new WeakChangeListener<>(selectionChangeListener));
		tblView.editingCellProperty().addListener(new WeakChangeListener<>(editingCellListener));
		tblView.agileEditingProperty().addListener(new WeakChangeListener<>(agileChangeListener));
		
		
		tblView.getChangedRecords().addListener(new WeakInvalidationListener(valueInvalidationListener));
		
	}
	
	private final InvalidationListener valueInvalidationListener = new InvalidationListener() {
		@Override
		public void invalidated(Observable observable) {
			pseudoClassStateChanged(PSEUDO_CLASS_CHANGED, tblView.getChangedRecords().contains(TableRowControl.this.getItem()));
		}
	};

	/**
	 * Handle row index change caused by scrolling the table. If the table is
	 * scrolled, the same TableRow object is reused but the position and the
	 * item ara changed. This listener change the display content of the row to
	 * prevent displaying cell editor controls in the wrong index.
	 */
	private void attachScrollListener() {
		this.itemProperty().addListener((ObservableValue<? extends R> observable, R oldValue, R newValue) -> {
			pseudoClassStateChanged(PSEUDO_CLASS_CHANGED, tblView.getChangedRecords().contains(TableRowControl.this.getItem()));
			if (tblView.getMode() == TableControl.Mode.READ || !tblView.isAgileEditing()) {
				return;
			}
			
//				pseudoClassStateChanged(PSEUDO_CLASS_CHANGED, tblView.getChangedRecords().contains(TableRowControl.this.getItem()));
			/**
			 * The previously selected row might have different row Index
			 * after it is scrolled back.
			 */
			if (selected) {
				selected = newValue == tblView.getSelectionModel().getSelectedItem();
				if (selected && tblView.isRecordEditable(getItem())) {
					setCellContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				} else {
					setCellContentDisplay(ContentDisplay.TEXT_ONLY);
				}
				
			} else if (newValue == tblView.getSelectionModel().getSelectedItem()) {
				selected = true;
				if (tblView.isRecordEditable(getItem())) {
					setCellContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				}
			}
		});
	}

	private void setCellContentDisplay(ContentDisplay contentDisplay) {
		if (this.getChildrenUnmodifiable() == null || this.getChildrenUnmodifiable().size() == 0) {
			Platform.runLater(() -> setCellContentDisplay(contentDisplay));
		} else {
			for (Node node : getChildrenUnmodifiable()) {
				TableCell cell = (TableCell) node;
				if (cell.getTableColumn().isEditable() || cell.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY) {
					cell.setContentDisplay(contentDisplay);
				}
			}
		}
	}

	public void refreshLookupSiblings(String propertyName) {

		for (Node node : getChildrenUnmodifiable()) {
			TableCell cell = (TableCell) node;
			if (cell.getTableColumn() instanceof BaseColumn) {
				BaseColumn baseColumn = (BaseColumn) cell.getTableColumn();
				if (baseColumn.getPropertyName().startsWith(propertyName) && !baseColumn.getPropertyName().equals(propertyName)) {
					ObservableValue currentObservableValue = baseColumn.getCellObservableValue(cell.getIndex());
					((BaseCell) cell).updateItem(currentObservableValue.getValue(), currentObservableValue == null);
				}
			}
		}
	}
	
	/////////////////////////////////////////////////////////
	// LISTENERS
	/////////////////////////////////////////////////////////
	/**
	 * If in normalEditing mode (agileEditing == false), when a particular cell
	 * is no longer being edited, the content display should change to TEXT_ONLY
	 */
	private ChangeListener<TablePosition<R, ?>> editingCellListener = new ChangeListener<TablePosition<R, ?>>() {
		@Override
		public void changed(ObservableValue<? extends TablePosition<R, ?>> observable, TablePosition<R, ?> oldValue, TablePosition<R, ?> newValue) {
			if ((newValue == null || newValue.getRow() == -1) && !tblView.isAgileEditing()) {
				setCellContentDisplay(ContentDisplay.TEXT_ONLY);
			}
		}
	};
	/**
	 * Flag the row whether it is editable or not.
	 * In Agile mode, display cell editors in selected row, if the record is editable.
	 */
	private ChangeListener<Number> selectionChangeListener = new ChangeListener<Number>() {
		@Override
		public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
			int idx = getIndex();
			if (!tblView.getTableView().isEditable() || (!t.equals(idx) && !t1.equals(idx))) {
				return; //nothing to do with me (i am a TableRowControl instance)
			}

			/**
			 * Set the row not editable if the record is not editable. This is
			 * just flag. The actual logic is in BaseCell.startEdit. The
			 * tableRowControl.isEditable is called by BaseCell.startEdit to
			 * decide whether it is okay to edit the cell or not.
			 */
			boolean isRecordEditable = true;
			if (!selected) {
				isRecordEditable = tblView.isRecordEditable(getItem());
				TableRowControl.this.setEditable(isRecordEditable);
			}

			if (tblView.isAgileEditing()) {
				if (t.equals(getIndex()) && selected) {
					setCellContentDisplay(ContentDisplay.TEXT_ONLY);
					selected = false;
				} else if (t1.equals(getIndex()) && !selected) {
					if (isRecordEditable) {
						setCellContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					}
					selected = true;
				}
			}
		}
	};
	/**
	 * Set content display to TEXT_ONLY in READ mode. This listener is relevant
	 * only in agileEditing mode
	 */
	private ChangeListener<TableControl.Mode> modeChangeListener = new ChangeListener<TableControl.Mode>() {
		@Override
		public void changed(ObservableValue<? extends TableControl.Mode> observable, TableControl.Mode oldValue, TableControl.Mode newValue) {
			if (newValue == TableControl.Mode.READ && selected) {
				selected = false;
				setCellContentDisplay(ContentDisplay.TEXT_ONLY);
			}
		}
	};
	private final ChangeListener<Boolean> agileChangeListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			if (tblView.modeProperty().get() == TableControl.Mode.READ) {
				return;
			}
			if (tblView.getSelectionModel().getSelectedIndex() == getIndex()) {
				if (newValue) {
					selected = true;
					if (tblView.isRecordEditable(getItem())) {
						setCellContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					}
				} else {
					selected = false;
					setCellContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}
	};
}
