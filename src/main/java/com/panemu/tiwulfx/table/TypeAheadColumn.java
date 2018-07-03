/*
 * Copyright (C) 2013 Panemu.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.panemu.tiwulfx.table;

import com.panemu.tiwulfx.common.TableCriteria.Operator;
import com.panemu.tiwulfx.control.TypeAheadField;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author amrullah
 */
public class TypeAheadColumn<R, C> extends BaseColumn<R, C>{
	private ObservableMap<String, C> itemMap = FXCollections.observableMap(new LinkedHashMap<String, C>());
	
	private TypeAheadField<C> searchInputControl = new TypeAheadField<>();
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
	
	public TypeAheadColumn() {
		this("");
	}

	public TypeAheadColumn(String propertyName) {
		this(propertyName, 100);
	}

	public TypeAheadColumn(String propertyName, double prefWidth) {
		super(propertyName, prefWidth);
		setCellFactory(new Callback<TableColumn<R, C>, TableCell<R, C>>() {
			@Override
			public TableCell<R, C> call(TableColumn<R, C> param) {
				return new TypeAheadTableCell<>(TypeAheadColumn.this);
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
		itemMap.remove(label);
	}
	
	/**
	 * Clear typeahead popup list
	 */
	public void clearItems() {
		itemMap.clear();
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
	
	private BooleanProperty sortedProperty = new SimpleBooleanProperty(false);
	/**
	 * 
	 * @return 
	 * @see #setSorted(boolean) 
	 */
	public boolean isSorted() {
		return sortedProperty.get();
	}
	
	/**
	 * Set whether items are sorted alphabetically. By Default is false which means the order of the items
	 * is based on the order they are registered to TypeAheadField.
	 * @param sorted set to true to sort the items. Default is false.
	 */
	public void setSorted(boolean sorted) {
		sortedProperty.set(sorted);
	}
	
	/**
	 * Property of sorted attribute
	 * @return 
	 * @see #setSorted(boolean) 
	 */
	public BooleanProperty sortedProperty() {
		return sortedProperty;
	}
	
}
