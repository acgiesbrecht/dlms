/*
 * License GNU LGPL
 * Copyright (C) 2013 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import java.util.List;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class TickColumn<R> extends TableColumn<R, Boolean> {

	private ReadOnlyListWrapper<R> tickedRecords = new ReadOnlyListWrapper<R>(FXCollections.<R>observableArrayList());
	private ReadOnlyListWrapper<R> untickedRecords = new ReadOnlyListWrapper<>(FXCollections.<R>observableArrayList());
	private CheckBox chkHeader = new CheckBox();

	public TickColumn() {
		super();
		setSortable(false);
		setGraphic(chkHeader);
		chkHeader.setSelected(defaultTicked.get());
		defaultTicked.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				chkHeader.setSelected(newValue);
			}
		});
		setText(null);
		setCellFactory(new Callback<TableColumn<R, Boolean>, TableCell<R, Boolean>>() {
			@Override
			public TableCell<R, Boolean> call(TableColumn<R, Boolean> param) {
				return new TickCell();
			}
		});
		setCellValueFactory(new Callback<TableColumn.CellDataFeatures<R, Boolean>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(CellDataFeatures<R, Boolean> param) {
				boolean val = defaultTicked.get();
				if (tickedRecords.contains(param.getValue())) {
					val = true;
				} else if (untickedRecords.contains(param.getValue())) {
					val = false;
				}
				return new SimpleBooleanProperty(val);
			}
		});

		tableViewProperty().addListener(new ChangeListener<TableView<R>>() {
			@Override
			public void changed(ObservableValue<? extends TableView<R>> observable, TableView<R> oldValue, TableView<R> newValue) {
				if (newValue != null) {
					/**
					 * The content of tickedRecords + untickedRecords should always
					 * equal with TableView's items
					 */
					getTableView().getItems().addListener(new ListChangeListener<R>() {
						@Override
						public void onChanged(Change<? extends R> change) {
							while (change.next()) {
								if (change.wasRemoved()) {
									untickedRecords.removeAll(change.getRemoved());
									tickedRecords.removeAll(change.getRemoved());
								} else if (change.wasAdded()) {
									if (defaultTicked.get()) {
										tickedRecords.get().addAll(change.getAddedSubList());
									} else {
										untickedRecords.get().addAll(change.getAddedSubList());
									}
								}
							}
						}
					});
				}
			}
		});

		chkHeader.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (chkHeader.isSelected()) {
					untickedRecords.clear();
					tickedRecords.setAll(getTableView().getItems());
				} else {
					tickedRecords.clear();
					untickedRecords.setAll(getTableView().getItems());
				}
				Set<Node> nodes = getTableView().lookupAll(".table-row-cell");
				for (Node node : nodes) {
					if (node instanceof TableRow) {
						TableRow<R> row = (TableRow) node;
						R item = row.getItem();
						row.setItem(null);
						row.setItem(item);
					}
				}
			}
		});
		chkHeader.disableProperty().bind(this.editableProperty().not());
	}
	private BooleanProperty defaultTicked = new SimpleBooleanProperty(false);

	public boolean isDefaultTicked() {
		return defaultTicked.get();
	}

	/**
	 * Sets whether the row is by default ticked or not
	 *
	 * @param ticked
	 */
	public void setDefaultTicked(boolean ticked) {
		defaultTicked.set(ticked);
	}

	/**
	 * Gets property of defaultTicked
	 *
	 * @return
	 */
	public BooleanProperty defaultTickedProperty() {
		return defaultTicked;
	}

	private void setHeaderSelected(boolean selected) {
		chkHeader.setSelected(selected);
	}

	/**
	 * Check if passed item is ticked
	 *
	 * @param item
	 * @return
	 */
	public Boolean isTicked(R item) {
		if (tickedRecords.contains(item)) {
			return true;
		}
		return false;
	}

	/**
	 * Set passed item to be ticked or unticked
	 *
	 * @param item
	 * @param value
	 */
	public void setTicked(R item, boolean value) {
		if (value) {
			untickedRecords.remove(item);
			if (!tickedRecords.contains(item)) {
				tickedRecords.add(item);
			}
		} else {
			tickedRecords.remove(item);
			if (!untickedRecords.contains(item)) {
				untickedRecords.add(item);
			}
		}
	}

	/**
	 * Gets tickedRecords property. This property is synchronized with
	 * {@link #untickedRecordsProperty()}.
	 *
	 * @return
	 */
	public ReadOnlyListProperty<R> tickedRecordsProperty() {
		return tickedRecords.getReadOnlyProperty();
	}

	/**
	 * Gets untickedRecords property. This property is synchronized with
	 * {@link #tickedRecordsProperty()}.
	 *
	 * @return
	 */
	public ReadOnlyListProperty<R> untickedRecordsProperty() {
		return untickedRecords.getReadOnlyProperty();
	}

	public List<R> getTickedRecords() {
		return tickedRecords.get();
	}

	public List<R> getUntickedRecords() {
		return untickedRecords.get();
	}

	private class TickCell extends TableCell<R, Boolean> {

		private CheckBox checkbox = new CheckBox();

		public TickCell() {
			super();
			checkbox.setDisable(!TickColumn.this.isEditable());
			TickColumn.this.editableProperty().addListener(new WeakChangeListener<>(editableListener));
			setGraphic(checkbox);
			setAlignment(Pos.BASELINE_CENTER);
			checkbox.setAlignment(Pos.CENTER);
			setText(null);
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			checkbox.setMaxWidth(Double.MAX_VALUE);
			contentDisplayProperty().addListener(new ChangeListener<ContentDisplay>() {
				private boolean suspendEvent = false;

				@Override
				public void changed(ObservableValue<? extends ContentDisplay> observable, ContentDisplay oldValue, ContentDisplay newValue) {
					if (suspendEvent) {
						return;
					}
					if (newValue != ContentDisplay.GRAPHIC_ONLY) {
						suspendEvent = true;
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						suspendEvent = false;
					}
				}
			});

			checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					setTicked((R) getTableRow().getItem(), newValue);
					if (!newValue) {
						setHeaderSelected(false);
					} else {
						setHeaderSelected(untickedRecords.isEmpty());
					}
				}
			});
		}

		@Override
		protected void updateItem(Boolean item, boolean empty) {
			super.updateItem(item, empty);
			if (!empty && getTableRow().getItem() != null) {
				setGraphic(checkbox);
				if (getTableRow() != null) {
					checkbox.setSelected(isTicked((R) getTableRow().getItem()));
				}
			} else {
				setGraphic(null);
			}
		}
		private ChangeListener<Boolean> editableListener = new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue != null) {
					checkbox.setDisable(!newValue);
				}
			}
		};
	}
}
