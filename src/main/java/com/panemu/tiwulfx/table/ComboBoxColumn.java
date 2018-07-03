/*
 * License GNU LGPL
 * Copyright (C) 2012 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TableCriteria.Operator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class ComboBoxColumn<R, C> extends BaseColumn<R, C> {

	private ObservableMap<String, C> itemMap = FXCollections.observableMap(new LinkedHashMap<String, C>());
	private ComboBox<C> searchInputControl = new ComboBox<>();
	private SearchMenuItemBase<C> searchMenuItem = new SearchMenuItemBase<C>(this) {
		@Override
		protected Node getInputControl() {
			return searchInputControl;
		}

		@Override
		protected List<Operator> getOperators() {
			List<Operator> lst = new ArrayList<>();
			lst.add(Operator.eq);
			lst.add(Operator.ne);
			lst.add(Operator.is_null);
			lst.add(Operator.is_not_null);
			return lst;
		}

		@Override
		protected C getValue() {
			return searchInputControl.getValue();
		}
	};

	public ComboBoxColumn() {
		this("");
	}

	public ComboBoxColumn(String propertyName) {
		this(propertyName, 100);
	}

	public ComboBoxColumn(String propertyName, double prefWidth) {
		super(propertyName, prefWidth);
		setCellFactory(new Callback<TableColumn<R, C>, TableCell<R, C>>() {
			@Override
			public TableCell<R, C> call(TableColumn<R, C> param) {
				return new ComboBoxTableCell<R,C>(ComboBoxColumn.this);
			}
		});
		searchInputControl.setConverter(stringConverter);
		searchInputControl.setFocusTraversable(false);

		setStringConverter(stringConverter);
		
		itemMap.addListener(new MapChangeListener<String, C>() {

			@Override
			public void onChanged(MapChangeListener.Change<? extends String, ? extends C> change) {
				if (change.wasAdded()) {
					searchInputControl.getItems().add(change.getValueAdded());
				} else if (change.wasRemoved()) {
					searchInputControl.getItems().remove(change.getValueAdded());
				}
			}
		});
	}

	@Override
	MenuItem getSearchMenuItem() {
		if (getDefaultSearchValue() != null) {
			searchInputControl.setValue(getDefaultSearchValue());
		}
		return searchMenuItem;
	}

	/**
	 * Add pair of label and object corresponding to the label
	 *
	 * @param label
	 * @param object
	 */
	public void addItem(String label, C object) {
		itemMap.put(label, object);
	}

	/**
	 * Remove an item from typeahead popup list. It has the opposite effect of 
	 * {@link #addItem(java.lang.String, java.lang.Object) addItem}
	 * @param label 
	 */
	public void removeItem(String label) {
		searchInputControl.getItems().remove(itemMap.get(label));
	}

	/**
	 * Clear typeahead popup list
	 */
	public void clearItems() {
		searchInputControl.getItems().clear();
	}
	
	/**
	 * Get ObservableMap (LinkedHashMap) of typeahead label-value pair popup list
	 * @return 
	 */
	public ObservableMap<String, C> getItemMap() {
		return itemMap;
	}

	private StringConverter<C> stringConverter = new StringConverter<C>() {
		@Override
		public String toString(C object) {
			if (object == null) {
				return getNullLabel();
			}
			Set<String> keys = itemMap.keySet();
			for (String label : keys) {
				C obj = itemMap.get(label);
				if (obj.equals(object)) {
					return label;
				}
			}
			return object.toString();
		}

		@Override
		public C fromString(String string) {
			if (string == null || string.equals(getNullLabel())) {
				return null;
			}
			return itemMap.get(string);
		}
	};

	
}
