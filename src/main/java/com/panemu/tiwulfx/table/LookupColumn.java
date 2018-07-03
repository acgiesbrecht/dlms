/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TableCriteria;
import com.panemu.tiwulfx.common.TiwulFXUtil;
import com.panemu.tiwulfx.control.LookupField;
import com.panemu.tiwulfx.control.LookupFieldController;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class LookupColumn<R, C> extends BaseColumn<R, C> {

	private TextField searchInputControl;
	private SearchMenuItemBase<C> searchMenuItem;
	private String lookupPropertyName;
	private LookupFieldController<C> lookupController;
	private boolean disableLookupManualInput = false;

	public LookupColumn() {
		this("", "");
	}

	public LookupColumn(String propertyName, String lookupPropertyName) {
		this(propertyName, lookupPropertyName, 100);
	}

	public LookupColumn(String propertyName, String lookupPropertyName, double prefWidth) {
		super(propertyName, prefWidth);
		this.lookupPropertyName = lookupPropertyName;
		setCellFactory(new Callback<TableColumn<R, C>, TableCell<R, C>>() {
			@Override
			public TableCell<R, C> call(TableColumn<R, C> p) {
				return new LookupTableCell<R, C>(LookupColumn.this);
			}
		});
		setStringConverter(stringConverter);
	}

	public String getLookupPropertyName() {
		return lookupPropertyName;
	}

	public void setLookupPropertyName(String lookupPropertyName) {
		this.lookupPropertyName = lookupPropertyName;
	}

	@Override
	MenuItem getSearchMenuItem() {
		return getLookupMenuItem();
	}

	public LookupFieldController<C> getLookupController() {
		return lookupController;
	}

	public void setLookupController(LookupFieldController<C> lookupController) {
		this.lookupController = lookupController;
	}
	private Map<String, C> mapValue = new HashMap<>();
	private StringConverter<C> stringConverter = new StringConverter<C>() {
		@Override
		public String toString(C value) {
			Object string;
			try {
				string = PropertyUtils.getSimpleProperty(value, lookupPropertyName);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
				return null;
			} catch (Exception ex) {
				return null;
			}
			return string != null ? string.toString() : getNullLabel();
		}

		@Override
		public C fromString(String stringValue) {
			if (stringValue == null || stringValue.equals(getNullLabel())) {
				return null;
			}
			C result = mapValue.get(stringValue);
			if (result == null && stringValue != null && !stringValue.isEmpty()) {
				List<C> data = lookupController.loadDataForPopup(getLookupPropertyName(), stringValue, TableCriteria.Operator.eq);
				if (data.size() == 1) {
					result = data.get(0);
					mapValue.put(stringValue, result);
				} else {
					result = getLookupController().show(getTableView().getScene().getWindow(), null, getLookupPropertyName(), stringValue);
				}

			}
			return result;
		}
	};

	private SearchMenuItemBase<C> getLookupMenuItem() {
		if (searchMenuItem == null) {
			searchInputControl = new TextField();
			searchMenuItem = new SearchMenuItemBase<C>(this) {
				@Override
				protected Node getInputControl() {
					return searchInputControl;
				}

				@Override
				protected List<TableCriteria.Operator> getOperators() {
					List<TableCriteria.Operator> lst = new ArrayList<>();
					lst.add(TableCriteria.Operator.eq);
					lst.add(TableCriteria.Operator.ne);
					lst.add(TableCriteria.Operator.ilike_begin);
					lst.add(TableCriteria.Operator.ilike_anywhere);
					lst.add(TableCriteria.Operator.ilike_end);
					lst.add(TableCriteria.Operator.lt);
					lst.add(TableCriteria.Operator.le);
					lst.add(TableCriteria.Operator.gt);
					lst.add(TableCriteria.Operator.ge);
					lst.add(TableCriteria.Operator.is_null);
					lst.add(TableCriteria.Operator.is_not_null);
					return lst;
				}

				@Override
				protected C getValue() {
					return (C) searchInputControl.getText();
				}
			};
		}
		C defaultSearch = getDefaultSearchValue();
		if (defaultSearch != null) {
			searchInputControl.setText(stringConverter.toString(defaultSearch));
		}
		return searchMenuItem;
	}
	private int waitTime = TiwulFXUtil.DEFAULT_LOOKUP_SUGGESTION_WAIT_TIMES;

	/**
	 * Set wait time in millisecond for showing suggestion list. Set it to -1 to
	 * disable suggestion list feature.
	 *
	 * @param waitTime Default is 500 millisecond
	 */
	public void setShowSuggestionWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}
	
	@Override
	TableCriteria<C> createSearchCriteria(TableCriteria.Operator operator, C value) {
		return new TableCriteria(this.getPropertyName() + "." + lookupPropertyName, operator, value);
	}

	public int getShowSuggestionWaitTime() {
		return this.waitTime;
	}

	/**
	 * Set it TRUE to restrict input only from lookupWindow and disable user
	 * manual input.
	 *
	 * @param disableLookupManualInput
	 */
	public void setDisableLookupManualInput(boolean disableLookupManualInput) {
		this.disableLookupManualInput = disableLookupManualInput;
	}

	public boolean getDisableLookupManualInput() {
		return disableLookupManualInput;
	}
}
