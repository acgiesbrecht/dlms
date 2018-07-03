/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.ExceptionHandler;
import com.panemu.tiwulfx.common.ExceptionHandlerFactory;
import com.panemu.tiwulfx.common.LiteralUtil;
import com.panemu.tiwulfx.common.TableCriteria;
import com.panemu.tiwulfx.common.TableData;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.dialog.MessageDialog;
import com.panemu.tiwulfx.dialog.MessageDialogBuilder;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author amrullah
 */
public class TableControl<R> extends VBox {

	private Button btnAdd;
	private Button btnEdit;
	private Button btnDelete;
	private Button btnFirstPage;
	private Button btnLastPage;
	private Button btnNextPage;
	private Button btnPrevPage;
	private Button btnReload;
	private Button btnSave;
	private Button btnExport;
	private ComboBox<Integer> cmbPage;
	private Label lblRowIndex;
	private Label lblTotalRow;
	private CustomTableView<R> tblView = new CustomTableView<>();
	private Region spacer;
	private HBox paginationBox;
	private ToolBar toolbar;
	private StackPane footer;
	private TableController<R> controller;
	private SimpleIntegerProperty startIndex = new SimpleIntegerProperty(0);
	private StartIndexChangeListener startIndexChangeListener = new StartIndexChangeListener();
	private InvalidationListener sortTypeChangeListener = new SortTypeChangeListener();
	private ReadOnlyObjectWrapper<Mode> mode = new ReadOnlyObjectWrapper<>(null);
	private long totalRows = 0;
	private Integer page = 1;
	private ObservableList<R> lstChangedRow = FXCollections.observableArrayList();
	private Class<R> recordClass;
	private boolean fitColumnAfterReload = false;
	private List<TableCriteria> lstCriteria = new ArrayList<>();
	private boolean reloadOnCriteriaChange = true;
	private boolean directEdit = false;
	private final ObservableList<TableColumn<R, ?>> columns = tblView.getColumns();
	private ProgressBar progressIndicator = new ProgressBar();
	private TableControlService service = new TableControlService();
	private MenuButton menuButton;
	private MenuItem resetItem;
	private String configurationID;
	private Logger logger = Logger.getLogger(LiteralUtil.class.getName());
	
	public static enum Mode {

		INSERT, EDIT, READ
	}

	/**
	 * UI component in TableControl which their visibility could be manipulated
	 *
	 * @see #setVisibleComponents(boolean,
	 * com.panemu.tiwulfx.table.TableControl.Component[])
	 *
	 */
	public static enum Component {

		BUTTON_RELOAD, BUTTON_INSERT, BUTTON_EDIT, BUTTON_SAVE, BUTTON_DELETE,
		BUTTON_EXPORT, BUTTON_PAGINATION, TOOLBAR, FOOTER;
	}

	public TableControl(Class<R> recordClass) {
		this.recordClass = recordClass;
		initControls();

		tblView.getSortOrder().addListener(new ListChangeListener<TableColumn<R, ?>>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TableColumn<R, ?>> change) {
				reload();
			}
		});

		btnAdd.disableProperty().bind(mode.isEqualTo(Mode.EDIT));
		btnEdit.disableProperty().bind(mode.isNotEqualTo(Mode.READ));
		btnSave.disableProperty().bind(mode.isEqualTo(Mode.READ));
		btnDelete.disableProperty().bind(new BooleanBinding() {
			{
				super.bind(mode, tblView.getSelectionModel().selectedItemProperty(), lstChangedRow);
			}

			@Override
			protected boolean computeValue() {
				if ((mode.get() == Mode.INSERT && lstChangedRow.size() < 2) || tblView.getSelectionModel().selectedItemProperty().get() == null || mode.get() == Mode.EDIT) {
					return true;
				}
				return false;
			}
		});
		tblView.editableProperty().bind(mode.isNotEqualTo(Mode.READ));
		tblView.getSelectionModel().cellSelectionEnabledProperty().bind(tblView.editableProperty());
		mode.addListener(new ChangeListener<Mode>() {
			@Override
			public void changed(ObservableValue<? extends Mode> ov, Mode t, Mode t1) {
				if (t1 == Mode.READ) {
					directEdit = false;
				}
			}
		});

		tblView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				lblRowIndex.setText(TiwulFXUtil.getLiteral("row.param", (page * maxResult.get() + t1.intValue() + 1)));
			}
		});

		tblView.getFocusModel().focusedCellProperty().addListener(new ChangeListener<TablePosition>() {
			@Override
			public void changed(ObservableValue<? extends TablePosition> observable, TablePosition oldValue, TablePosition newValue) {
				if (tblView.isEditable() && directEdit && agileEditing.get()) {
					tblView.edit(newValue.getRow(), newValue.getTableColumn());
				}
			}
		});

		tblView.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ESCAPE) {
					directEdit = false;
				} else if (event.getCode() == KeyCode.ENTER && mode.get() == Mode.READ) {
					getController().doubleClick(getSelectionModel().getSelectedItem());
				}
			}
		});

		/**
		 * Define policy for TAB key press
		 */
		tblView.addEventFilter(KeyEvent.KEY_PRESSED, tableKeyListener);
		/**
		 * In INSERT mode, only inserted row that is focusable
		 */
		tblView.getFocusModel().focusedCellProperty().addListener(tableFocusListener);

      tblView.setOnMouseReleased(tableRightClickListener);
      
      if (Platform.isFxApplicationThread()) {
         cm = new ContextMenu();
         cm.setAutoHide(true);
         setToolTips();
         /**
          * create custom row factory that can intercept double click on grid row
          */
         tblView.setRowFactory(new Callback<TableView<R>, TableRow<R>>() {
            @Override
            public TableRow<R> call(TableView<R> param) {
               return new TableRowControl(TableControl.this);
            }
         });
      }

		columns.addListener(new ListChangeListener<TableColumn<R, ?>>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends TableColumn<R, ?>> change) {
				while (change.next()) {
					if (change.wasAdded()) {
						for (TableColumn<R, ?> column : change.getAddedSubList()) {
							initColumn(column);
						}
					}
					lastColumnIndex = getLeafColumns().size() - 1;
				}
			}
		});
		attachWindowVisibilityListener();
	}

	private int lastColumnIndex = 0;

	public final ObservableList<TableColumn<R, ?>> getColumns() {
		return columns;
	}

	public ObservableList<R> getChangedRecords() {
		return lstChangedRow;
	}

	private void initControls() {
		this.getStyleClass().add("table-control");
		btnAdd = buildButton(TiwulFXUtil.getGraphicFactory().createAddGraphic());
		btnDelete = buildButton(TiwulFXUtil.getGraphicFactory().createDeleteGraphic());
		btnEdit = buildButton(TiwulFXUtil.getGraphicFactory().createEditGraphic());
		btnExport = buildButton(TiwulFXUtil.getGraphicFactory().createExportGraphic());
		btnReload = buildButton(TiwulFXUtil.getGraphicFactory().createReloadGraphic());
		btnSave = buildButton(TiwulFXUtil.getGraphicFactory().createSaveGraphic());

		btnFirstPage = new Button();
		btnFirstPage.setGraphic(TiwulFXUtil.getGraphicFactory().createPageFirstGraphic());
		btnFirstPage.setOnAction(paginationHandler);
		btnFirstPage.setDisable(true);
		btnFirstPage.setFocusTraversable(false);
		btnFirstPage.getStyleClass().addAll("pill-button", "pill-button-left");
		
		btnPrevPage = new Button();
		btnPrevPage.setGraphic(TiwulFXUtil.getGraphicFactory().createPagePrevGraphic());
		btnPrevPage.setOnAction(paginationHandler);
		btnPrevPage.setDisable(true);
		btnPrevPage.setFocusTraversable(false);
		btnPrevPage.getStyleClass().addAll("pill-button", "pill-button-center");

		btnNextPage = new Button();
		btnNextPage.setGraphic(TiwulFXUtil.getGraphicFactory().createPageNextGraphic());
		btnNextPage.setOnAction(paginationHandler);
		btnNextPage.setDisable(true);
		btnNextPage.setFocusTraversable(false);
		btnNextPage.getStyleClass().addAll("pill-button", "pill-button-center");
		
		btnLastPage = new Button();
		btnLastPage.setGraphic(TiwulFXUtil.getGraphicFactory().createPageLastGraphic());
		btnLastPage.setOnAction(paginationHandler);
		btnLastPage.setDisable(true);
		btnLastPage.setFocusTraversable(false);
		btnLastPage.getStyleClass().addAll("pill-button", "pill-button-right");
		
		cmbPage = new ComboBox<>();
		cmbPage.setEditable(true);
		cmbPage.setOnAction(paginationHandler);
		cmbPage.setFocusTraversable(false);
		cmbPage.setDisable(true);
		cmbPage.getStyleClass().addAll("combo-page");
		cmbPage.setPrefWidth(75);

		paginationBox = new HBox();
		paginationBox.setAlignment(Pos.CENTER);
		paginationBox.getChildren().addAll(btnFirstPage, btnPrevPage, cmbPage, btnNextPage, btnLastPage);

		spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		toolbar = new ToolBar(btnReload, btnAdd, btnEdit, btnSave, btnDelete, btnExport, spacer, paginationBox);
		toolbar.getStyleClass().add("table-toolbar");

		footer = new StackPane();
		footer.getStyleClass().add("table-footer");
		lblRowIndex = new Label();
		lblTotalRow = new Label();
		menuButton = new TableControlMenu(this);
		StackPane.setAlignment(lblRowIndex, Pos.CENTER_LEFT);
		StackPane.setAlignment(lblTotalRow, Pos.CENTER);
		StackPane.setAlignment(menuButton, Pos.CENTER_RIGHT);
		
		lblTotalRow.visibleProperty().bind(progressIndicator.visibleProperty().not());

		progressIndicator.setProgress(-1);
		progressIndicator.visibleProperty().bind(service.runningProperty());
		toolbar.disableProperty().bind(service.runningProperty());
		menuButton.disableProperty().bind(service.runningProperty());

		footer.getChildren().addAll(lblRowIndex, lblTotalRow, menuButton, progressIndicator);
		VBox.setVgrow(tblView, Priority.ALWAYS);
		getChildren().addAll(toolbar, tblView, footer);

	}

	private Button buildButton(Node graphic) {
		Button btn = new Button();
		btn.setGraphic(graphic);
		btn.getStyleClass().add("flat-button");
		btn.setOnAction(buttonHandler);
		return btn;
	}
	private EventHandler<ActionEvent> buttonHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == btnAdd) {
				insert();
			} else if (event.getSource() == btnDelete) {
				delete();
			} else if (event.getSource() == btnEdit) {
				edit();
			} else if (event.getSource() == btnExport) {
				export();
			} else if (event.getSource() == btnReload) {
				reload();
			} else if (event.getSource() == btnSave) {
				save();
			}
		}
	};
	private EventHandler<ActionEvent> paginationHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if (event.getSource() == btnFirstPage) {
				reloadFirstPage();
			} else if (event.getSource() == btnPrevPage) {
				prevPageFired(event);
			} else if (event.getSource() == btnNextPage) {
				nextPageFired(event);
			} else if (event.getSource() == btnLastPage) {
				lastPageFired(event);
			} else if (event.getSource() == cmbPage) {
				pageChangeFired(event);
			}
		}
	};

	/**
	 * Set selection mode
	 *
	 * @see javafx.scene.control.SelectionMode
	 * @param mode
	 */
	public void setSelectionMode(SelectionMode mode) {
		tblView.getSelectionModel().setSelectionMode(mode);
	}

	private void setToolTips() {
		TiwulFXUtil.setToolTip(btnAdd, "add.record");
		TiwulFXUtil.setToolTip(btnDelete, "delete.record");
		TiwulFXUtil.setToolTip(btnEdit, "edit.record");
		TiwulFXUtil.setToolTip(btnFirstPage, "go.to.first.page");
		TiwulFXUtil.setToolTip(btnLastPage, "go.to.last.page");
		TiwulFXUtil.setToolTip(btnNextPage, "go.to.next.page");
		TiwulFXUtil.setToolTip(btnPrevPage, "go.to.prev.page");
		TiwulFXUtil.setToolTip(btnReload, "reload.records");
		TiwulFXUtil.setToolTip(btnExport, "export.records");
		TiwulFXUtil.setToolTip(btnSave, "save.record");
	}
	private BooleanProperty agileEditing = new SimpleBooleanProperty(true);

	public void setAgileEditing(boolean agileEditing) {
		this.agileEditing.set(agileEditing);
	}

	public boolean isAgileEditing() {
		return agileEditing.get();
	}

	public BooleanProperty agileEditingProperty() {
		return agileEditing;
	}
	/**
	 * Move focus to the next cell if user pressing TAB and the mode is
	 * EDIT/INSERT
	 */
	private EventHandler<KeyEvent> tableKeyListener = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent event) {
			if (mode.get() == Mode.READ) {
				return;
			}
			if (event.getCode() == KeyCode.TAB) {
				if (event.isShiftDown()) {
					if (tblView.getSelectionModel().getSelectedCells().get(0).getColumn() == 0) {
						List<TableColumn<R, ?>> leafColumns = getLeafColumns();
						tblView.getSelectionModel().select(tblView.getSelectionModel().getSelectedIndex() - 1, leafColumns.get(leafColumns.size() - 1));
					} else {
						tblView.getSelectionModel().selectLeftCell();
					}
				} else {
					if (tblView.getSelectionModel().getSelectedCells().get(0).getColumn() == lastColumnIndex) {
						tblView.getSelectionModel().select(tblView.getSelectionModel().getSelectedIndex() + 1, tblView.getColumns().get(0));
					} else {
						tblView.getSelectionModel().selectRightCell();
					}
				}
				horizontalScroller.run();
				event.consume();
			} else if (event.getCode() == KeyCode.ENTER && !event.isControlDown() && !event.isAltDown() && !event.isShiftDown()) {
				if (agileEditing.get()) {
					if (directEdit) {
						//TODO warning, using tblView.lookup is fragile
						showRow(tblView.getSelectionModel().getSelectedIndex() + 1);
						if (tblView.getSelectionModel().getSelectedIndex() == tblView.getItems().size() - 1) {
							//it will trigger cell's commit edit for the most bottom row
							tblView.getSelectionModel().selectAboveCell();
						}
						tblView.getSelectionModel().selectBelowCell();
						event.consume();
					} else {
						directEdit = true;
					}
				}
			} else if (event.getCode() == KeyCode.V && event.isControlDown()) {
				if (!isACellInEditing()) {
					paste();
					event.consume();
				}

			}
		}
	};

	private boolean isACellInEditing() {
		return tblView.getEditingCell() != null && tblView.getEditingCell().getRow() > -1;
	}

	public void showRow(int index) {
		//TODO warning, using tblView.lookup is fragile
		VirtualFlow node = (VirtualFlow) tblView.lookup("VirtualFlow");
		node.show(index);
	}

	/**
	 * Mark record as changed. It will only add the record to the changed record
	 * list if the record doesn't exist in the list. Avoid adding record to
	 * {@link #getChangedRecords()} to avoid adding the same record multiple
	 * times.
	 *
	 * @param record
	 */
	public void markAsChanged(R record) {
		if (!lstChangedRow.contains(record)) {
			lstChangedRow.add(record);
		}
	}

	/**
	 * Paste text on clipboard. Doesn't work on READ mode.
	 */
	public void paste() {
		if (mode.get() == Mode.READ) {
			return;
		}
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		if (clipboard.hasString()) {
			final String text = clipboard.getString();
			if (text != null) {
				List<TablePosition> cells = tblView.getSelectionModel().getSelectedCells();
				if (cells.isEmpty()) {
					return;
				}
				TablePosition cell = cells.get(0);
				List<TableColumn<R, ?>> lstColumn = getLeafColumns();
				TableColumn startColumn = null;
				for (TableColumn clm : lstColumn) {
					if (clm instanceof BaseColumn && clm == cell.getTableColumn()) {
						startColumn = (BaseColumn) clm;break;
					}
				}
				if (startColumn == null) {
					return;
				}
				int rowIndex = cell.getRow();
				String[] arrString = text.split("\n");
				boolean stopPasting = false;
				for (String line : arrString) {
					if (stopPasting) {
						break;
					}
					R item = null;
					if (rowIndex < tblView.getItems().size()) {
						item = tblView.getItems().get(rowIndex);
					} else if (mode.get() == Mode.EDIT) {
						/**
						 * Will ensure the content display to TEXT_ONLY because
						 * there is no way to update cell editors value (in
						 * agile editing mode)
						 */
						tblView.getSelectionModel().clearSelection();
						return;//stop pasting as it already touched last row
					}

					if (!lstChangedRow.contains(item)) {
						if (mode.get() == Mode.INSERT) {
							//means that selected row is not new row. Let's create new row
							createNewRow(rowIndex);
							item = tblView.getItems().get(rowIndex);
						} else {
							lstChangedRow.add(item);
						}
					}

					showRow(rowIndex);
					/**
					 * Handle multicolumn paste
					 */
					String[] stringCellValues = line.split("\t");
					TableColumn toFillColumn = startColumn;
					tblView.getSelectionModel().select(rowIndex, toFillColumn);
					for (String stringCellValue : stringCellValues) {
						if (toFillColumn == null) {
							break;
						}
						if (toFillColumn instanceof BaseColumn && toFillColumn.isEditable() && toFillColumn.isVisible()) {
							try {
								Object oldValue = toFillColumn.getCellData(item);
								Object newValue = ((BaseColumn) toFillColumn).convertFromString(stringCellValue);
								PropertyUtils.setSimpleProperty(item, ((BaseColumn) toFillColumn).getPropertyName(), newValue);
								if (mode.get() == Mode.EDIT) {
									((BaseColumn) toFillColumn).addRecordChange(item, oldValue, newValue);
								}
							} catch (Exception ex) {
								MessageDialog.Answer answer = MessageDialogBuilder
										.error(ex)
										.message("msg.paste.error", stringCellValue, toFillColumn.getText())
										.buttonType(MessageDialog.ButtonType.YES_NO)
										.yesOkButtonText("continue.pasting")
										.noButtonText("stop")
										.show(getScene().getWindow());
								if (answer == MessageDialog.Answer.NO) {
									stopPasting = true;
									break;
								}
							}
						}
						tblView.getSelectionModel().selectRightCell();
						TablePosition nextCell = tblView.getSelectionModel().getSelectedCells().get(0);
						if (nextCell.getTableColumn() instanceof BaseColumn && nextCell.getTableColumn() != toFillColumn) {
							toFillColumn = (BaseColumn) nextCell.getTableColumn();
						} else {
							toFillColumn = null;
						}
					}
					rowIndex++;
				}
				
				refresh();

				/**
				 * Will ensure the content display to TEXT_ONLY because there is
				 * no way to update cell editors value (in agile editing mode)
				 */
				tblView.getSelectionModel().clearSelection();
			}
		}
	}
	
	/**
	 * Force the table to repaint each row. What this method doing is set
	 * the row content to null and then set it back with row's original value.
	 * That is the way to trigger {@link Cell#updateItem(java.lang.Object, boolean) updateItem}
	 * of every cell in the row.
	 */
	public void refresh() {
		Set<Node> nodes = tblView.lookupAll(".table-row-cell");
		for (Node node : nodes) {
			if (node instanceof TableRowControl) {
				TableRowControl<R> row = (TableRowControl) node;
				R item = row.getItem();
				row.setItem(null);
				row.setItem(item);
			}
		}
	}
	
	/**
	 * Force the table to repaint specified row. What this method doing is set
	 * the row content to null and then set it back with row's original value.
	 * That is the way to trigger {@link Cell#updateItem(java.lang.Object, boolean) updateItem}
	 * of every cell in the row.
	 */
	public void refresh(R record) {
		Set<Node> nodes = tblView.lookupAll(".table-row-cell");
		for (Node node : nodes) {
			if (node instanceof TableRowControl) {
				TableRowControl<R> row = (TableRowControl) node;
				if (row.getItem() != null && row.getItem().equals(record)) {
					row.setItem(null);
					row.setItem(record);
				}
			}
		}
	}

	private Runnable horizontalScroller = new Runnable() {
		private ScrollBar scrollBar = null;
		@Override
		public void run() {
			TableView.TableViewFocusModel fm = tblView.getFocusModel();
			if (fm == null) {
				return;
			}

			TableColumn col = fm.getFocusedCell().getTableColumn();
			if (col == null || !col.isVisible()) {
				return;
			}
			if (scrollBar == null) {
				for (Node n : tblView.lookupAll(".scroll-bar")) {
					if (n instanceof ScrollBar) {
						ScrollBar bar = (ScrollBar) n;
						if (bar.getOrientation().equals(Orientation.HORIZONTAL) && bar.isVisible()) {
							scrollBar = bar;
							break;
						}
					}
				}
			}
			if (scrollBar == null) {
				//scrollbar is not visible, meaning all columns are visible.
				//No need to scroll
				return;
			}
			// work out where this column header is, and it's width (start -> end)
			double start = 0;
			for (TableColumn c : tblView.getVisibleLeafColumns()) {
				if (c.equals(col)) {
					break;
				}
				start += c.getWidth();
			}
			double end = start + col.getWidth();

			// determine the width of the table
			double headerWidth = tblView.getWidth() - tblView.snappedLeftInset() - tblView.snappedRightInset();
			
			// determine by how much we need to translate the table to ensure that
			// the start position of this column lines up with the left edge of the
			// tableview, and also that the columns don't become detached from the
			// right edge of the table
			double pos = scrollBar.getValue();
			double max = scrollBar.getMax();
			double newPos = pos;

			if (start < pos && start >= 0) {
				newPos = start;
			} else {
				double delta = start < 0 || end > headerWidth ? start - pos : 0;
            newPos = pos + delta > max ? max : pos + delta;
			}

			// FIXME we should add API in VirtualFlow so we don't end up going
			// direct to the hbar.
			// actually shift the flow - this will result in the header moving
			// as well
			scrollBar.setValue(newPos);
		}
	};

	/**
	 * Get single selected record property. If multiple records are selected, it
	 * returns the last one
	 *
	 * @return
	 */
	public ReadOnlyObjectProperty<R> selectedItemProperty() {
		return tblView.getSelectionModel().selectedItemProperty();
	}

	/**
	 * @see #selectedItemProperty()
	 * @return
	 */
	public R getSelectedItem() {
		return tblView.getSelectionModel().selectedItemProperty().get();
	}

	/**
	 * @see TableView#getSelectionModel()#getSelectionItems()
	 * @return
	 */
	public ObservableList<R> getSelectedItems() {
		return tblView.getSelectionModel().getSelectedItems();
	}

	/**
	 *
	 * @see TableView#getSelectionModel()
	 */
	public TableView.TableViewSelectionModel getSelectionModel() {
		return tblView.getSelectionModel();
	}
	/**
	 * Prevent moving focus to not-inserted-row in INSERT mode
	 */
	private ChangeListener<TablePosition> tableFocusListener = new ChangeListener<TablePosition>() {
		@Override
		public void changed(ObservableValue<? extends TablePosition> observable, TablePosition oldValue, TablePosition newValue) {
			if (!Mode.INSERT.equals(mode.get()) || newValue.getRow() == -1 || oldValue.getRow() == -1) {
				return;
			}

			Runnable runnable = new Runnable() {
				public void run() {
					R oldRow = tblView.getItems().get(oldValue.getRow());
					R newRow = tblView.getItems().get(newValue.getRow());
					if (lstChangedRow.contains(oldRow) && !lstChangedRow.contains(newRow)) {
						tblView.getFocusModel().focus(oldValue);
						tblView.getSelectionModel().select(oldValue.getRow(), oldValue.getTableColumn());
					}
				}
			};

			Platform.runLater(runnable);
		}
	};
	private ContextMenu cm;
	private MenuItem searchMenuItem;
	private EventHandler<MouseEvent> tableRightClickListener = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			if (cm.isShowing()) {
				cm.hide();
			}
			if (event.getButton().equals(MouseButton.SECONDARY)) {

				if (tblView.getSelectionModel().getSelectedCells().isEmpty()) {
					return;
				}
				//TablePosition pos = tblView.getSelectionModel().getSelectedCells().get(0);
				TableColumn<R, ?> column = tblView.getSelectedColumn();
				if (searchMenuItem != null) {
					cm.getItems().remove(searchMenuItem);
				}
				cm.getItems().remove(getPasteMenuItem());
				if (column instanceof BaseColumn) {
					BaseColumn clm = (BaseColumn) column;
					int row = tblView.getSelectionModel().getSelectedIndex();
					clm.setDefaultSearchValue(column.getCellData(row));

					searchMenuItem = clm.getSearchMenuItem();
					if (searchMenuItem != null && clm.isFilterable()) {
						cm.getItems().add(0, searchMenuItem);
					}
					if (mode.get() != Mode.READ && !isACellInEditing() && Clipboard.getSystemClipboard().hasString()) {
						if (!cm.getItems().contains(getPasteMenuItem())) {
							cm.getItems().add(getPasteMenuItem());
						}
					}
				}
				cm.show(tblView, event.getScreenX(), event.getScreenY());
			}
		}
	};
	private MenuItem miPaste;

	private MenuItem getPasteMenuItem() {
		if (miPaste == null) {
			miPaste = new MenuItem(TiwulFXUtil.getLiteral("paste"));
			miPaste.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					paste();
				}
			});
		}
		return miPaste;
	}

	/**
	 * Add menu item to context menu. It will be displayed under search menu
	 * item
	 *
	 * @param label the label of menu item
	 * @param eventHandler event handler that will be executed when the menu
	 * item is clicked
	 * @deprecated use
	 * {@link #addContextMenuItem(javafx.scene.control.MenuItem)} instead
	 */
	public void addContextMenuItem(String label, EventHandler<ActionEvent> eventHandler) {
		MenuItem mi = new MenuItem(label);
		mi.setOnAction(eventHandler);
		cm.getItems().add(mi);
	}

	/**
	 * Add menu item to context menu. The context menu is displayed when
	 * right-clicking a row.
	 *
	 * @param menuItem
	 * @see #removeContextMenuItem(javafx.scene.control.MenuItem)
	 */
	public void addContextMenuItem(MenuItem menuItem) {
		cm.getItems().add(menuItem);
	}

	/**
	 * Remove passed menuItem from context menu.
	 *
	 * @param menuItem
	 * @see #addContextMenuItem(javafx.scene.control.MenuItem)
	 */
	public void removeContextMenuItem(MenuItem menuItem) {
		cm.getItems().remove(menuItem);
	}

	protected void resizeToFit(TableColumn col, int maxRows) {
		List<?> items = tblView.getItems();
		if (items == null || items.isEmpty()) {
			return;
		}

		Callback cellFactory = col.getCellFactory();
		if (cellFactory == null) {
			return;
		}

		TableCell cell = (TableCell) cellFactory.call(col);
		if (cell == null) {
			return;
		}

		// set this property to tell the TableCell we want to know its actual
		// preferred width, not the width of the associated TableColumn
		cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE);

		// determine cell padding
		double padding = 10;
		Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
		if (n instanceof Region) {
			Region r = (Region) n;
			padding = r.getInsets().getLeft() + r.getInsets().getRight();
		}

		int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows);
		double maxWidth = 0;
		for (int row = 0; row < rows; row++) {
			cell.updateTableColumn(col);
			cell.updateTableView(tblView);
			cell.updateIndex(row);

			if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
				getChildren().add(cell);
				cell.impl_processCSS(false);
				maxWidth = Math.max(maxWidth, cell.prefWidth(-1));
				getChildren().remove(cell);
			}
		}

		col.impl_setWidth(maxWidth + padding);
	}

	public TableControl() {
		this(null);
	}

	/**
	 *
	 * @return Object set from
	 * {@link #setController(com.panemu.tiwulfx.table.TableController)}
	 */
	public TableController getController() {
		return controller;
	}

	/**
	 * Set object responsible to fetch, insert, delete and update data
	 *
	 * @param controller
	 */
	public void setController(TableController<R> controller) {
		this.controller = controller;
	}

	private void initColumn(TableColumn<R, ?> clm) {
		List<TableColumn<R, ?>> lstColumn = new ArrayList<>();
		lstColumn.add(clm);
		lstColumn = getColumnsRecursively(lstColumn);
		for (TableColumn column : lstColumn) {
			if (column instanceof BaseColumn) {
				final BaseColumn baseColumn = (BaseColumn) column;
				baseColumn.tableCriteriaProperty().addListener(tableCriteriaListener);
				baseColumn.sortTypeProperty().addListener(sortTypeChangeListener);
				baseColumn.addEventHandler(TableColumn.editCommitEvent(), new EventHandler<TableColumn.CellEditEvent<R, ?>>() {
					@Override
					public void handle(CellEditEvent<R, ?> t) {
						try {
							if (!(t.getTablePosition().getRow() < t.getTableView().getItems().size())) {
								return;
							}
							Object persistentObj = t.getTableView().getItems().get(t.getTablePosition().getRow());
							if ((t.getNewValue() == null && t.getOldValue() == null)
									  || (t.getNewValue() != null && t.getNewValue().equals(t.getOldValue()))) {
							}
							if (mode.get().equals(Mode.EDIT)
									  && t.getOldValue() != t.getNewValue()
									  && (t.getOldValue() == null || !t.getOldValue().equals(t.getNewValue()))) {
								if (!lstChangedRow.contains((R) persistentObj)) {
									lstChangedRow.add((R) persistentObj);
								}
								baseColumn.addRecordChange(persistentObj, t.getOldValue(), t.getNewValue());
							}
							PropertyUtils.setSimpleProperty(persistentObj, baseColumn.getPropertyName(), t.getNewValue());
							baseColumn.validate(persistentObj);
						} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
							throw new RuntimeException(ex);
						}
					}
				});

			}
		}
	}

	/**
	 * Add column to TableView. You can also call {@link TableView#getColumns()}
	 * and then add columns to it.
	 *
	 * @param columns
	 */
	public void addColumn(TableColumn<R, ?>... columns) {
		tblView.getColumns().addAll(columns);
	}

	/**
	 * Get list of columns including the nested ones.
	 *
	 * @param lstColumn
	 * @return
	 */
	private List<TableColumn<R, ?>> getColumnsRecursively(List<TableColumn<R, ?>> lstColumn) {
		List<TableColumn<R, ?>> newColumns = new ArrayList<>();
		for (TableColumn column : lstColumn) {
			if (column.getColumns().isEmpty()) {
				newColumns.add(column);
			} else {
				/**
				 * Should be in new arraylist to avoid
				 * java.lang.IllegalArgumentException: Children: duplicate
				 * children added
				 */
				newColumns.addAll(getColumnsRecursively(new ArrayList(column.getColumns())));
			}
		}
		return newColumns;
	}

	/**
	 * Get list of columns that is hold cell. It excludes columns that are
	 * containers of nested columns.
	 *
	 * @return
	 */
	public List<TableColumn<R, ?>> getLeafColumns() {
		List<TableColumn<R, ?>> result = new ArrayList<>();
		for (TableColumn<R, ?> clm : tblView.getColumns()) {
			if (clm.getColumns().isEmpty()) {
				result.add(clm);
			} else {
				result.addAll(getColumnsRecursively(clm.getColumns()));
			}
		}
		return result;
	}

	/**
	 * Clear all criteria/filters applied to columns then reload the first page.
	 */
	public void clearTableCriteria() {
		setReloadOnCriteriaChange(false);
		for (TableColumn clm : getLeafColumns()) {
			if (clm instanceof BaseColumn) {
				((BaseColumn) clm).setTableCriteria(null);
			}
		}
		setReloadOnCriteriaChange(true);
		reloadFirstPage();
	}

	/**
	 * Reload data on current page. This method is called when pressing reload
	 * button TODO: put it in separate thread and display progress indicator on
	 * TableView
	 *
	 * @see #reloadFirstPage()
	 */
	public void reload() {
		if (!lstChangedRow.isEmpty()) {
			if (!controller.revertConfirmation(this, lstChangedRow.size())) {
				return;
			}
		}
		lstCriteria.clear();
		/**
		 * Should be in new arraylist to avoid
		 * java.lang.IllegalArgumentException: Children: duplicate children
		 * added
		 */
		List<TableColumn<R, ?>> lstColumns = new ArrayList<>(tblView.getColumns());
		lstColumns = getColumnsRecursively(lstColumns);
		for (TableColumn clm : lstColumns) {
			if (clm instanceof BaseColumn) {
				BaseColumn baseColumn = (BaseColumn) clm;
				if (baseColumn.getTableCriteria() != null) {
					lstCriteria.add(baseColumn.getTableCriteria());
				}
			}
		}
		List<String> lstSortedColumn = new ArrayList<>();
		List<SortType> lstSortedType = new ArrayList<>();
		for (TableColumn<R, ?> tc : tblView.getSortOrder()) {
			if (tc instanceof BaseColumn) {
				lstSortedColumn.add(((BaseColumn) tc).getPropertyName());
				lstSortedType.add(tc.getSortType());
			} else if (tc.getCellValueFactory() instanceof PropertyValueFactory) {
				PropertyValueFactory valFactory = (PropertyValueFactory) tc.getCellValueFactory();
				lstSortedColumn.add(valFactory.getProperty());
				lstSortedType.add(tc.getSortType());
			}
		}
		
		service.runLoadInBackground(lstSortedColumn, lstSortedType);
	}

	private void clearChange() {
		lstChangedRow.clear();
		for (TableColumn clm : getLeafColumns()) {
			if (clm instanceof BaseColumn) {
				((BaseColumn) clm).clearRecordChange();
			}
		}
	}

	/**
	 * Get list of change happens on cells. It is useful to get detailed
	 * information of old and new values of particular record's property
	 *
	 * @return
	 */
	public List<RecordChange<R, ?>> getRecordChangeList() {
		List<RecordChange<R, ?>> lstRecordChange = new ArrayList<>();
		for (TableColumn column : getLeafColumns()) {
			if (column instanceof BaseColumn) {
				BaseColumn<R, ?> baseColumn = (BaseColumn) column;
				Map<R, RecordChange<R, ?>> map = (Map<R, RecordChange<R, ?>>) baseColumn.getRecordChangeMap();
				lstRecordChange.addAll(map.values());
			}
		}
		return lstRecordChange;
	}

	private void toggleButtons(boolean notEmpty, boolean moreRows) {
		boolean firstPage = startIndex.get() == 0;
		btnFirstPage.setDisable(firstPage && notEmpty);
		btnPrevPage.setDisable(firstPage && notEmpty);
		btnNextPage.setDisable(!moreRows);
		btnLastPage.setDisable(!moreRows);
	}

	/**
	 * Reload data from the first page.
	 *
	 */
	public void reloadFirstPage() {
		if (startIndex.get() != 0) {
			/**
			 * it will automatically reload data. See StartIndexChangeListener
			 */
			startIndex.set(0);
		} else {
			reload();
		}
	}

	private void lastPageFired(ActionEvent event) {
		cmbPage.getSelectionModel().selectLast();
	}

	private void prevPageFired(ActionEvent event) {
		cmbPage.getSelectionModel().selectPrevious();
	}

	private void nextPageFired(ActionEvent event) {
		cmbPage.getSelectionModel().selectNext();
	}

	private void pageChangeFired(ActionEvent event) {
		if (cmbPage.getValue() != null) {
			page = Integer.valueOf(cmbPage.getValue() + "");//since the combobox is editable, it might have String value
			page = page - 1;
			startIndex.set(page * maxResult.get());
		}
	}

	/**
	 * Return false if the insertion is canceled because the controller return
	 * null object. It is controller's way to abort insertion.
	 *
	 * @param rowIndex
	 * @return
	 */
	private void createNewRow(int rowIndex) {
		R newRecord;
		try {
			newRecord = (R) Class.forName(recordClass.getName()).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}

		if (tblView.getItems().size() == 0) {
			rowIndex = 0;
		}

		tblView.getItems().add(rowIndex, newRecord);
		lstChangedRow.add(newRecord);
	}

	/**
	 * Add new row under selected row or in the first row if there is no row
	 * selected. This method is called when pressing insert button
	 */
	public void insert() {
		if (recordClass == null) {
			throw new RuntimeException("Cannot add new row because the class of the record is undefined.\nPlease call setRecordClass(Class<T> recordClass)");
		}
		R newRecord;
		try {
			newRecord = (R) Class.forName(recordClass.getName()).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			RuntimeException re = new RuntimeException("Failed to instantiate " + recordClass.getName() + " reflexively. Ensure it has an empty constructor.", ex);
			throw re;
		}
		newRecord = controller.preInsert(newRecord);
		if (newRecord == null) {
			return;
		}

		int selectedRow = tblView.getSelectionModel().getSelectedIndex() + 1;
		if (tblView.getItems().size() == 0) {
			selectedRow = 0;
		}

		tblView.getItems().add(selectedRow, newRecord);
		lstChangedRow.add(newRecord);
		final int row = selectedRow;
		mode.set(Mode.INSERT);

		/**
		 * Force the table to layout before selecting the newly added row.
		 * Without this call, the selection will land on existing row at
		 * specified index because the new row is not yet actually added to the
		 * table. It makes the editor controls are not displayed in agileEditing
		 * mode.
		 */
		tblView.layout();
		tblView.requestFocus();
		showRow(row);
		tblView.getSelectionModel().select(row, tblView.getColumns().get(0));
	}

	/**
	 * Save changes. This method is called when pressing save button
	 *
	 */
	public void save() {
		/**
		 * In case there is a cell being edited, call clearSelection() to trigger
		 * commitEdit() in the edited cell.
		 */
		tblView.getSelectionModel().clearSelection();

		try {
			if (lstChangedRow.isEmpty()) {
				mode.set(Mode.READ);
				return;
			}
			if (!controller.validate(this, lstChangedRow)) {
				return;
			}
			Mode prevMode = mode.get();
			service.runSaveInBackground(prevMode);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

	/**
	 * Edit table. This method is called when pressing edit button.
	 *
	 */
	public void edit() {
		if (controller.canEdit(tblView.getSelectionModel().getSelectedItem())) {
			mode.set(Mode.EDIT);
		}
	}

	/**
	 * Delete selected row. This method is called when pressing delete button.
	 * It will delete selected record(s)
	 *
	 */
	public void delete() {
		/**
		 * Delete row that is not yet persisted in database.
		 */
		if (mode.get() == Mode.INSERT) {
			TablePosition selectedCell = tblView.getSelectionModel().getSelectedCells().get(0);
			int selectedRow = selectedCell.getRow();
			lstChangedRow.removeAll(tblView.getSelectionModel().getSelectedItems());
			tblView.getSelectionModel().clearSelection();// it is needed if agile editing is enabled to trigger content display change later
			tblView.getItems().remove(selectedRow);
			tblView.layout();//relayout first before set selection. Without this, cell contend display won't be set propertly
			tblView.requestFocus();
			if (selectedRow == tblView.getItems().size()) {
				selectedRow--;
			}
			if (lstChangedRow.contains(tblView.getItems().get(selectedRow))) {
				tblView.getSelectionModel().select(selectedRow, selectedCell.getTableColumn());
			} else {
				tblView.getSelectionModel().select(selectedRow - 1, selectedCell.getTableColumn());
			}
			return;
		}

		/**
		 * Delete persistence record.
		 */
		try {
			if (!controller.canDelete(this)) {
				return;
			}
			int selectedRow = tblView.getSelectionModel().getSelectedIndex();
			List<R> lstToDelete = new ArrayList<>(tblView.getSelectionModel().getSelectedItems());
			
			service.runDeleteInBackground(lstToDelete, selectedRow);
			
		} catch (Exception ex) {
			handleException(ex);
		}
	}

	/**
	 * Export table to Excel. All pages will be exported. The criteria set on
	 * columns are taken into account. This method is called by export button.
	 */
	public void export() {
		//controller.exportToExcel("Override TableController.exportToExcel to reset the title.", maxResult.get(), this, lstCriteria);
		service.runExportInBackground();
	}

	private class StartIndexChangeListener implements ChangeListener<Number> {

		@Override
		public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
			reload();
		}
	}
	private InvalidationListener tableCriteriaListener = new InvalidationListener() {
		@Override
		public void invalidated(Observable observable) {
			if (reloadOnCriteriaChange) {
				reloadFirstPage();
			}
		}
	};

	private class SortTypeChangeListener implements InvalidationListener {

		@Override
		public void invalidated(Observable o) {
			/**
			 * If the column is not in sortOrder list, just ignore. It avoids
			 * intermittent duplicate reload() calling
			 */
			TableColumn col = (TableColumn) ((SimpleObjectProperty) o).getBean();
			if (!tblView.getSortOrder().contains(col)) {
				return;
			}
			reload();
		}
	}

	private IntegerProperty maxResult = new SimpleIntegerProperty(TiwulFXUtil.DEFAULT_TABLE_MAX_ROW);

	/**
	 * Set max record per retrieval
	 *
	 * @param maxRecord
	 */
	public void setMaxRecord(int maxRecord) {
		this.maxResult.set(maxRecord);
	}

	/**
	 * Get max number of records per-retrieval.
	 *
	 * @return
	 */
	public int getMaxRecord() {
		return maxResult.get();
	}

	public IntegerProperty maxRecordProperty() {
		return maxResult;
	}

	/**
	 *
	 * @return Class object set on {@link #setRecordClass(java.lang.Class) }
	 * @see #setRecordClass(java.lang.Class)
	 */
	public Class<R> getRecordClass() {
		return recordClass;
	}

	/**
	 * Set the class of object that will be displayed in the table.
	 *
	 * @param recordClass
	 */
	public void setRecordClass(Class<R> recordClass) {
		this.recordClass = recordClass;
	}

	public void setFitColumnAfterReload(boolean fitColumnAfterReload) {
		this.fitColumnAfterReload = fitColumnAfterReload;
	}

	/**
	 *
	 * @return @see #setReloadOnCriteriaChange(boolean)
	 */
	public boolean isReloadOnCriteriaChange() {
		return reloadOnCriteriaChange;
	}

	/**
	 * Set it to false to prevent auto-reloading when there is table criteria
	 * change. It is useful if we want to change tableCriteria of several
	 * columns at a time. After that set it to true and call {@link TableControl#reloadFirstPage()
	 * }
	 *
	 * @param reloadOnCriteriaChange
	 */
	public void setReloadOnCriteriaChange(boolean reloadOnCriteriaChange) {
		this.reloadOnCriteriaChange = reloadOnCriteriaChange;
	}

	/**
	 * Get displayed record. It is just the same with
	 * {@link TableView#getItems()}
	 *
	 * @return
	 */
	public ObservableList<R> getRecords() {
		return tblView.getItems();
	}

	private void setOrNot(ToolBar parent, Node control, boolean visible) {
		if (!visible) {
			parent.getItems().remove(control);
		} else if (!parent.getItems().contains(control)) {
			parent.getItems().add(control);
		}
	}

	/**
	 * Add button to toolbar. The button's style is set by this method. Make
	 * sure to add image on the button and also define the action method.
	 *
	 * @param btn
	 */
	public void addButton(Button btn) {
		addNode(btn);
	}
	
	/**
	 * Add JavaFX Node to table's toolbar
	 * @param node 
	 */
	public void addNode(Node node) {
		if (node instanceof Button) {
			node.getStyleClass().add("flat-button");
		}
		boolean hasPagination = toolbar.getItems().contains(paginationBox);
		if (hasPagination) {
			toolbar.getItems().remove(spacer);
			toolbar.getItems().remove(paginationBox);
		}
		toolbar.getItems().add(node);
		if (hasPagination) {
			toolbar.getItems().add(spacer);
			toolbar.getItems().add(paginationBox);
		}
	}

	/**
	 * Set UI component visibility.
	 *
	 * @param visible
	 * @param controls
	 */
	public void setVisibleComponents(boolean visible, TableControl.Component... controls) {
		for (Component comp : controls) {
			switch (comp) {
				case BUTTON_DELETE:
					setOrNot(toolbar, btnDelete, visible);
					break;
				case BUTTON_EDIT:
					setOrNot(toolbar, btnEdit, visible);
					break;
				case BUTTON_INSERT:
					setOrNot(toolbar, btnAdd, visible);
					break;
				case BUTTON_EXPORT:
					setOrNot(toolbar, btnExport, visible);
					break;
				case BUTTON_PAGINATION:
					setOrNot(toolbar, spacer, visible);
					setOrNot(toolbar, paginationBox, visible);
					break;
				case BUTTON_RELOAD:
					setOrNot(toolbar, btnReload, visible);
					break;
				case BUTTON_SAVE:
					setOrNot(toolbar, btnSave, visible);
					break;
				case FOOTER:
					if (!visible) {
						this.getChildren().remove(footer);
					} else if (!this.getChildren().contains(footer)) {
						this.getChildren().add(footer);
					}
					break;
				case TOOLBAR:
					if (!visible) {
						this.getChildren().remove(toolbar);
					} else if (!this.getChildren().contains(toolbar)) {
						this.getChildren().add(0, toolbar);
					}
					break;
			}
		}
	}

	public Mode getMode() {
		return mode.get();
	}

	public ReadOnlyObjectProperty<Mode> modeProperty() {
		return mode.getReadOnlyProperty();
	}

	public TableView<R> getTableView() {
		return tblView;
	}

	public final ReadOnlyObjectProperty<TablePosition<R, ?>> editingCellProperty() {
		return tblView.editingCellProperty();
	}

	/**
	 * Check if a record is editable. After ensure that the item is not null and
	 * the mode is not {@link Mode#INSERT} it will propagate the call to
	 * {@link TableController#isRecordEditable}.
	 *
	 * @see TableController#isRecordEditable()
	 * @param item
	 * @return false if item == null. True if mode is INSERT. otherwise depends
	 * on the logic in {@link TableController#isRecordEditable}
	 */
	public final boolean isRecordEditable(R item) {
		if (item == null) {
			return false;
		}
		if (mode.get() == Mode.INSERT) {
			return true;
		}
		return controller.isRecordEditable(item);
	}
	
	private void postLoadAction(TableData<R> vol) {
		if (vol.getRows() == null) {
			vol.setRows(new ArrayList<>());
		}
		
		totalRows = vol.getTotalRows();

		//keep track of previous selected row
		int selectedIndex = tblView.getSelectionModel().getSelectedIndex();
		TableColumn selectedColumn = null;
		if (!tblView.getSelectionModel().getSelectedCells().isEmpty()) {
			selectedColumn = tblView.getSelectionModel().getSelectedCells().get(0).getTableColumn();
		}

		if (isACellInEditing()) {
			/**
			 * Trigger cancelEdit if there is cell being edited. Otherwise
			 * ArrayIndexOutOfBound exception happens since tblView items are
			 * cleared (see next lines) but setOnEditCommit listener is executed.
			 */
			tblView.edit(-1, tblView.getColumns().get(0));
		}

		//clear items and add with objects that has just been retrieved
		tblView.getItems().clear();
		tblView.getItems().addAll(vol.getRows());

		if (selectedIndex < vol.getRows().size()) {
			tblView.getSelectionModel().select(selectedIndex, selectedColumn);
		} else {
			tblView.getSelectionModel().select(vol.getRows().size() - 1, selectedColumn);
		}

		long page = vol.getTotalRows() / maxResult.get();
		if (vol.getTotalRows() % maxResult.get() != 0) {
			page++;
		}
		cmbPage.setDisable(page == 0);
		startIndex.removeListener(startIndexChangeListener);
		cmbPage.getItems().clear();
		for (int i = 1; i <= page; i++) {
			cmbPage.getItems().add(i);
		}
		cmbPage.getSelectionModel().select((int) (startIndex.get() / maxResult.get()));
		startIndex.addListener(startIndexChangeListener);
		toggleButtons(page > 0, vol.isMoreRows());

		mode.set(Mode.READ);

		clearChange();
		if (fitColumnAfterReload) {
			for (TableColumn clm : tblView.getColumns()) {
				resizeToFit(clm, -1);
			}
		}
		lblTotalRow.setText(TiwulFXUtil.getLiteral("total.record.param", totalRows));

		for (TableColumn clm : getLeafColumns()) {
			if (clm instanceof BaseColumn) {
				((BaseColumn) clm).getInvalidRecordMap().clear();
			}
		}
		controller.postLoadData();
	}
	
	/**
	 * Set a callback to override default error handling. By default, the 
	 * error is displayed in an error message. The error source might be a failed
	 * load, save and delete actions.
	 * @param callback 
	 * @deprecated this function does nothing. Please call {@link #setExceptionHandler(com.panemu.tiwulfx.common.ExceptionHandler) setExceptionHandler} instead
	 */
	@Deprecated
	public void setErrorHandlerCallback(Callback<Throwable, Void> callback) {
	}
	
	

	private void postSaveAction(List<R> lstResult, Mode prevMode) {
		mode.set(Mode.READ);
		/**
		 * In case objects in lstResult differ with original object. Ex: In SOA
		 * architecture, sent objects always differ with received object due to
		 * serialization.
		 */
		int i = 0;
		for (R row : lstChangedRow) {
			int index = tblView.getItems().indexOf(row);
			tblView.getItems().remove((int) index);
			tblView.getItems().add(index, lstResult.get(i));
			i++;
		}

		/**
		 * Refresh cells. They won't refresh automatically if the entity's
		 * properties bound to the cells are not javaFX property object.
		 */
		clearChange();
		controller.postSave(prevMode);
	}
	
	private void postDeleteAction(List<R> lstDeleted, int selectedRow) {
		tblView.getItems().removeAll(lstDeleted);
		/**
		 * select a row
		 */
		if (!tblView.getItems().isEmpty()) {
			if (selectedRow >= tblView.getItems().size()) {
				tblView.getSelectionModel().select(tblView.getItems().size() - 1);
			} else {
				tblView.getSelectionModel().select(selectedRow);
			}
		}
		totalRows = totalRows - lstDeleted.size();
		lblTotalRow.setText(TiwulFXUtil.getLiteral("total.record.param", totalRows));
		tblView.requestFocus();
	}
	
	private void saveColumnPosition() {
		if (configurationID == null || configurationID.trim().length() == 0) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Map<String, String> mapProperties = new HashMap<>();
				for (TableColumn column : columns) {
					int oriIndex = lstTableColumnsOriginalOrder.indexOf(column);
					int newIndex = columns.indexOf(column);
					mapProperties.put(configurationID + "." + oriIndex + ".pos", newIndex + "");
				}
				try {
					TiwulFXUtil.writeProperties(mapProperties);
				} catch (Exception ex) {
					handleException(ex);
				}
			}
		};
		new Thread(runnable).start();
		configureResetMenuItem();
	}
	
	private void configureResetMenuItem() {
		if (resetItem == null) {
			resetItem = new MenuItem(TiwulFXUtil.getLiteral("reset.columns"));
			resetItem.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent t) {
					resetColumnPosition();
				}
			});
		}
		if (!menuButton.getItems().contains(resetItem)) {
			menuButton.getItems().add(resetItem);
		}
	}
	
	private void attachWindowVisibilityListener() {
		this.sceneProperty().addListener(new ChangeListener<Scene>() {

			@Override
			public void changed(ObservableValue<? extends Scene> ov, Scene t, Scene scene) {
				if (scene != null) {
					/**
					 * TODO Once this code is executed, the scene listener need to be removed
					 * to avoid potential memory leak.
					 * This code need to be initialized once only.
					 */
					readColumnPosition();
					columns.addListener(new ListChangeListener<TableColumn<R, ?>>() {
						@Override
						public void onChanged(ListChangeListener.Change<? extends TableColumn<R, ?>> change) {
							while (change.next()) {
								if (change.wasReplaced()) {
									saveColumnPosition();
								}
							}
						}
					});
					if (configurationID != null && configurationID.trim().length() != 0) {
						for (final TableColumn clm : getColumns()) {
							clm.widthProperty().addListener(new ChangeListener<Number>() {
								@Override
								public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
									int clmIdx = lstTableColumnsOriginalOrder.indexOf(clm);
									try {
										TiwulFXUtil.writeProperties(configurationID + "." + clmIdx + ".width", newValue + "");
									} catch (Exception ex) {
										logger.log(Level.WARNING, "Unable to save column width information. Column index: " + clmIdx, ex);
									}
								}
							});
						}
					}
				}
			}

		});
	}

	private void resetColumnPosition() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				List<String> propNames = new ArrayList<>();
				for (int i = 0; i < columns.size(); i++) {
					propNames.add(configurationID + "." + i + ".pos");
				}
				
				try {
					TiwulFXUtil.deleteProperties(propNames);
				} catch (Exception ex) {
					handleException(ex);
				}
			}
		};
		new Thread(runnable).start();
		if (menuButton.getItems().contains(resetItem)) {
			menuButton.getItems().remove(resetItem);
		}
		columns.clear();
		columns.addAll(lstTableColumnsOriginalOrder);
	}
	
	private List<TableColumn<R,?>> lstTableColumnsOriginalOrder;
	private void readColumnPosition() {
		lstTableColumnsOriginalOrder = new ArrayList<>(columns);
		if (configurationID == null || configurationID.trim().length() == 0) {
			return;
		}
		
		Map<Integer, TableColumn> map = new HashMap<>();
		int maxIndex = 0;
		for (int i = 0; i < columns.size(); i++) {
			String pos = TiwulFXUtil.readProperty(configurationID + "." + i + ".pos");
			if (pos != null && pos.matches("[0-9]*")) {
				map.put(Integer.valueOf(pos), columns.get(i));
				maxIndex = Math.max(maxIndex, Integer.valueOf(pos));
			}
			String width = TiwulFXUtil.readProperty(configurationID + "." + i + ".width");
			if (width != null && width.matches("\\d*\\.?\\d*")) {
				double widthDouble = Double.valueOf(width);
				if (widthDouble > 5.0) {
					columns.get(i).setPrefWidth(widthDouble);
				}
			}
		}
		if (!map.isEmpty()) {
			columns.clear();

			for (int i = 0; i <= maxIndex; i++) {
				TableColumn tc = map.get(i);
				if (tc != null) {
					columns.add(i, tc);
				}
			}

			configureResetMenuItem();
		}
	}

	/**
	 * Get configuration ID.
	 * @return 
	 * @see #setConfigurationID(java.lang.String) for detailed explanation
	 */
	public String getConfigurationID() {
		return configurationID;
	}

	/**
	 * If it is set, the table position will be saved to a configuration file
	 * located in a folder inside user's home directory. Call {@link TiwulFXUtil#setApplicationId(java.lang.String)}
	 * to set the folder name. The configurationID must be unique across all TableControl in an application but it is
	 * not enforced.
	 * 
	 * @param configurationID must be unique across all TableControls in an application
	 * @see TiwulFXUtil#setApplicationId(java.lang.String) 
	 */
	public void setConfigurationID(String configurationID) {
		this.configurationID = configurationID;
	}
	
	private ExceptionHandler exceptionHandler = TiwulFXUtil.getExceptionHandler();

	/**
	 * Get {@link ExceptionHandler}. The default is what returned by
	 * {@link TiwulFXUtil#getExceptionHandler()}
	 * <p>
	 * @return an implementation of ExceptionHandler that is called when
	 *         uncatched exception happens
	 */
	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * Set custom {@link ExceptionHandler} to override the default taken from
	 * {@link TiwulFXUtil#getExceptionHandler()}. To override application-wide
	 * {@link ExceptionHandler} call
	 * {@link TiwulFXUtil#setExceptionHandlerFactory(ExceptionHandlerFactory)}
	 * <p>
	 * @param exceptionHandler custom Exception Handler
	 */
	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}
	
	private void handleException(Throwable throwable) {
		Window window = null;
		if (getScene() != null) {
			window = getScene().getWindow();
		}
		exceptionHandler.handleException(throwable, window);
	}
	
	private class TableControlService extends Service {

		private List<String> lstSortedColumn = new ArrayList<>();
		private List<SortType> sortingOrders = new ArrayList<>();
		private Mode prevMode;
		private int actionCode;
		private List<R> lstToDelete;
		private int selectedRow;
		
		public void runLoadInBackground(List<String> lstSortedColumn, List<SortType> sortingOrders) {
			this.lstSortedColumn = lstSortedColumn;
			this.sortingOrders = sortingOrders;
			actionCode = 0;
			this.restart();
		}
		
		public void runSaveInBackground(Mode prevMode) {
			this.prevMode = prevMode;
			actionCode = 1;
			this.restart();
		}
		
		public void runDeleteInBackground(List<R> lstToDelete, int selectedRow) {
			this.lstToDelete = lstToDelete;
			this.selectedRow = selectedRow;
			actionCode = 2;
			this.restart();
		}
		
		public void runExportInBackground() {
			actionCode = 3;
			this.restart();
		}

		@Override
		protected Task createTask() {
			if (actionCode == 0) {
				return new LoadDataTask(lstSortedColumn, sortingOrders);
			} else if (actionCode == 1) {
				return new SaveTask(prevMode);
			} else if (actionCode == 2) {
				return new DeleteTask(lstToDelete, selectedRow);
			} else if (actionCode == 3) {
				return new ExportTask();
			}
			return null;
		}

	}

	private class LoadDataTask extends Task<TableData<R>> {

		private List<String> lstSortedColumn = new ArrayList<>();
		private List<SortType> sortingOrders = new ArrayList<>();

		public LoadDataTask(List<String> sortedColumns,
				List<SortType> sortingOrders) {

			this.lstSortedColumn = sortedColumns;
			this.sortingOrders = sortingOrders;
			
			setOnFailed((WorkerStateEvent event) -> {
				handleException(getException());
			});
			
			setOnSucceeded((WorkerStateEvent event) -> {
				TableData<R> vol = getValue();
				postLoadAction(vol);
			});
		}

		@Override
		protected TableData<R> call() throws Exception {
			
			return controller.loadData(startIndex.get(), lstCriteria,
					lstSortedColumn, sortingOrders, maxResult.get());
			
		}

	}
	
	private class SaveTask extends Task<List<R>> {
		private Mode prevMode;
		public SaveTask(Mode prevMode) {
			this.prevMode = prevMode;
			setOnFailed((WorkerStateEvent event) -> {
				handleException(getException());
			});
			
			setOnSucceeded((WorkerStateEvent event) -> {
				postSaveAction(getValue(), prevMode);
			});
		}

		@Override
		protected List<R> call() throws Exception {
			List<R> lstResult = new ArrayList<>();
			if (mode.get().equals(Mode.EDIT)) {
				lstResult = controller.update(lstChangedRow);
			} else if (mode.get().equals(Mode.INSERT)) {
				lstResult = controller.insert(lstChangedRow);
			}
			return lstResult;
		}
		
	}
	
	private class DeleteTask extends Task<Void> {
		private final List<R> lstToDelete;
		
		public DeleteTask(List<R> lstToDelete, int selectedRow) {
			this.lstToDelete = lstToDelete;
			
			setOnFailed((WorkerStateEvent event) -> {
				handleException(getException());
			});
			
			setOnSucceeded((WorkerStateEvent event) -> {
				postDeleteAction(lstToDelete, selectedRow);
			});
		}
		
		@Override
		protected Void call() throws Exception {
			controller.delete(lstToDelete);
			return null;
		}
		
	}
	
	private class ExportTask extends Task<Void> {

		public ExportTask() {
			setOnFailed((WorkerStateEvent event) -> {
				handleException(getException());
			});
		}

		@Override
		protected Void call() throws Exception {
			controller.exportToExcel("Override TableController.exportToExcel to reset the title.", maxResult.get(), TableControl.this, lstCriteria);
			return null;
		}
		
	}
}
