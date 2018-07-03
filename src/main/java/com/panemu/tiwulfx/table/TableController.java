/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.ExportToExcel;
import com.panemu.tiwulfx.common.TableCriteria;
import com.panemu.tiwulfx.common.TableData;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.dialog.MessageDialog;
import com.panemu.tiwulfx.dialog.MessageDialogBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for {@link TableControl}. The
 * {@link #loadData(int, java.util.List, java.util.List, java.util.List, int)}
 * is mandatory to be implemented. That method should handle data population for
 * {@link TableControl}
 *
 * @author amrullah
 */
public abstract class TableController<R> {

	public abstract TableData<R> loadData(int startIndex, List<TableCriteria> filteredColumns, List<String> sortedColumns, List<SortType> sortingOrders, int maxResult);

	/**
	 * Override this method to implement insert routine. This method is called
	 * when TableControl's save button is clicked.
	 *
	 * @param newRecords
	 * @return
	 */
	public List<R> insert(List<R> newRecords) {
		throw new UnsupportedOperationException("Insert method is not supported");
	}

	/**
	 * Override this method to implement update routine.
	 *
	 * @param records
	 * @return updated record list. If the record support optimistic locking the
	 * version number should increase by 1
	 */
	public List<R> update(List<R> records) {
		throw new UnsupportedOperationException("Update method is not supported");
	}

	/**
	 * Override this method to implement delete routine.
	 *
	 * @param records
	 */
	public void delete(List<R> records) {
		throw new UnsupportedOperationException("delete method is not supported");
	}

	/**
	 * Called after user clicked insert button before a new row is added in the
	 * table. To cancel insertion, return null. This method is useful for setting
	 * default values to the new record.
	 *
	 * @param newRecord
	 * @return null to cancel insertion
	 */
	public R preInsert(R newRecord) {
		return newRecord;
	}

	/**
	 * This method is called by {@link TableControl} when edit button is clicked
	 * but before EDIT mode is actually activated. Override this method and
	 * return FALSE to cancel EDIT mode change.
	 *
	 * @param selectedRecord
	 * @return FALSE to cancel the EDIT mode. By default returns TRUE
	 */
	public boolean canEdit(R selectedRecord) {
		return true;
	}

	/**
	 * This method is called by {@link TableControl#delete()} before it actually
	 * executes delete routine. If this method returns FALSE, the delete is
	 * canceled.
	 *
	 * @param table
	 * @return
	 */
	public boolean canDelete(TableControl<R> table) {
		MessageDialog.Answer answer = MessageDialogBuilder.confirmation()
				  .message(TiwulFXUtil.getLiteral("msg.delete.confirmation"))
				  .title(TiwulFXUtil.getLiteral("msg.delete.confirmation.title"))
				  .defaultAnswer(MessageDialog.Answer.NO)
				  .yesOkButtonText("delete.confirmation.delete")
				  .noButtonText("delete.confirmation.dont-delete")
				  .show(table.getScene().getWindow());
		return answer.equals(MessageDialog.Answer.YES_OK);
	}

	/**
	 * Callback method to respond double click or ENTER on table's row
	 *
	 * @param record
	 */
	public void doubleClick(R record) {
	}

	/**
	 * Generic export to excel. This method is called by clicking TableControl's
	 * Export button. You can override this method to define title in the
	 * generated spreadsheet.
	 *
	 * @param title
	 * @param maxResult
	 * @param tblView
	 * @param lstCriteria
	 */
	public void exportToExcel(String title, int maxResult, TableControl<R> tblView, List<TableCriteria> lstCriteria) {
		try {
			ExportToExcel exporter = new ExportToExcel();
			List<Double> lstWidth = new ArrayList<>();
			List<R> data = new ArrayList<>();
			List<String> lstSortedColumn = new ArrayList<>();
			List<SortType> lstSortedType = new ArrayList<>();

			for (TableColumn<R, ?> tc : tblView.getTableView().getSortOrder()) {
				if (tc instanceof BaseColumn) {
					lstSortedColumn.add(((BaseColumn) tc).getPropertyName());
				} else {
					PropertyValueFactory valFactory = (PropertyValueFactory) tc.getCellValueFactory();
					lstSortedColumn.add(valFactory.getProperty());
				}
				lstSortedType.add(tc.getSortType());
			}

			TableData vol;
			int startIndex2 = 0;

			do {
				vol = loadData(startIndex2, lstCriteria, lstSortedColumn, lstSortedType, maxResult);
				data.addAll((ObservableList<R>) FXCollections.observableArrayList(vol.getRows()));
				startIndex2 = startIndex2 + maxResult;
			} while (vol.isMoreRows());

			String tmpFolder = System.getProperty("java.io.tmpdir");
			File targetFile = File.createTempFile("test", ".xls", new File(tmpFolder));
			exporter.export(title, targetFile.getAbsolutePath(), tblView, data, lstWidth);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Callback method that is called after {@link #loadData(int, java.util.List, java.util.List, java.util.List, int)
	 * }
	 */
	public void postLoadData() {
	}

	/**
	 * Callback method that is called after {@link TableControl#save() }.
	 *
	 * @param previousMode Could be Mode.INSERT or Mode.EDIT
	 */
	public void postSave(TableControl.Mode previousMode) {
	}

	protected void displayInvalidErrorMessage(TableControl<R> tbl) {
		MessageDialogBuilder.error().message("invalid.values.save.aborted").show(tbl.getScene().getWindow());
	}
	
	/**
	 * Callback method designed to execute validation. This method is called
	 * after user click Save button before calling {@link #update} and
	 * {@link #insert}. By default, this method triggers
	 * {@link BaseColumn#validate} and returns Boolean.True
	 *
	 * @param changedRecords
	 * @return false if there is invalid value
	 */
	public boolean validate(TableControl<R> tbl, List<R> changedRecords) {
		boolean result = true;
		for (TableColumn clm : tbl.getLeafColumns()) {
			if (clm instanceof BaseColumn) {
				BaseColumn baseColumn = (BaseColumn) clm;
				for (R record : changedRecords) {
					boolean isValid = baseColumn.validate(record);
					if (!isValid) {
						result = isValid;
					}
				}
			}
		}
		if (!result) {
			displayInvalidErrorMessage(tbl);
		}
		return result;
	}

	/**
	 * A callback that is called before reloading table and there is record
	 * changed. This method will show a warning dialog telling user that some
	 * records are changed, and asking user to revert & reload, or cancel reload.
	 *
	 * @param table
	 * @param numberOfChangedRows the number of changed record
	 * @return true will revert change and continue loading data. Return false
	 * will cancel reloading data.
	 */
	protected boolean revertConfirmation(TableControl table, int numberOfChangedRows) {
		MessageDialog.Answer answer = MessageDialogBuilder.warning().message("reload.confirmation", numberOfChangedRows)
				  .buttonType(MessageDialog.ButtonType.YES_NO)
				  .yesOkButtonText("revert.then.reload")
				  .noButtonText("cancel.reload")
				  .show(table.getScene().getWindow());
		return answer == MessageDialog.Answer.YES_OK;
	}

	public boolean isRecordEditable(R item) {
		return true;
	}
}
