/*
 * License GNU LGPL
 * Copyright (C) 2013 Amrullah <amrullah@panemu.com>.
 */
package com.panemu.tiwulfx.control.sidemenu;

import com.panemu.tiwulfx.common.TiwulFXUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.WindowEvent;

/**
 *
 * @author Amrullah <amrullah@panemu.com>
 */
public class SideMenu extends VBox {

	private List<SideMenuItem> lstMenuItem = new ArrayList<SideMenuItem>();
	private BooleanProperty expanded = new SimpleBooleanProperty(true);
	private BooleanProperty armed = new SimpleBooleanProperty(false);
	private StackPane expStackPane = new StackPane();
	private VBox expMenuContainer = new VBox();
	private Button btnExpand = new Button();
	private Popup popup;
	private VBox infoBox;
	private Label infoName;
	private Map<TitledPane, VBox> map = new HashMap<TitledPane, VBox>();
	private Node imgCollapse = TiwulFXUtil.getGraphicFactory().createMenuCollapseGraphic();
	private Node imgExpand = TiwulFXUtil.getGraphicFactory().createMenuExpandGraphic();
	private SideMenuActionHandler facade;
	private boolean toggleExpand = true;

	public SideMenu() {
		super();
		btnExpand.graphicProperty().bind(Bindings.when(expanded).then(imgCollapse).otherwise(imgExpand));
		expMenuContainer.setAlignment(Pos.TOP_CENTER);
		btnExpand.getStyleClass().add("collapse-button");
		StackPane.setMargin(btnExpand, new Insets(2, 2, 0, 0));
		StackPane.setAlignment(btnExpand, Pos.TOP_RIGHT);

		expStackPane.getChildren().addAll(expMenuContainer, btnExpand);
		VBox.setVgrow(expStackPane, Priority.ALWAYS);
		expStackPane.getStyleClass().add("side-menu-container");
		this.setMinWidth(50);
		this.getChildren().add(expStackPane);
		btnExpand.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				expanded.set(!expanded.get());
			}
		});
		expanded.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				for (Node node : expMenuContainer.getChildren()) {
					if (node instanceof TitledPane) {
						TitledPane pane = (TitledPane) node;
						pane.setContentDisplay(newValue ? ContentDisplay.LEFT : ContentDisplay.GRAPHIC_ONLY);
						if (newValue) {
							pane.setContent(map.get(pane));
						} else {
							pane.setContent(null);
						}
						pane.setCollapsible(newValue);
					} else if (node instanceof Button) {
						Button btn = (Button) node;
						btn.setContentDisplay(newValue ? ContentDisplay.LEFT : ContentDisplay.GRAPHIC_ONLY);
					}
				}
				if (newValue) {
					expStackPane.getChildren().add(btnExpand);
				} else {
					expMenuContainer.getChildren().add(0, btnExpand);
				}
			}
		});

	}
	
	public BooleanProperty expandedProperty() {
		return expanded;
	}

	public SideMenuActionHandler getFacade() {
		return facade;
	}

	public void setActionHandler(SideMenuActionHandler facade) {
		this.facade = facade;
	}

	private Popup getPopup() {
		if (popup == null) {
			infoBox = new VBox();
			infoBox.setId("side-menu-popup");
			infoBox.setFillWidth(true);
			infoBox.setMinWidth(USE_PREF_SIZE);
//            infoBox.setPrefWidth(350);
			infoName = new Label();
			infoName.setId("side-menu-popup-header");
			infoName.setMinHeight(USE_PREF_SIZE);
			infoName.setPrefHeight(28);
			infoBox.getChildren().addAll(infoName);

			popup = new Popup();
			popup.getContent().setAll(infoBox);
			popup.setAutoHide(true);
			popup.setConsumeAutoHidingEvents(false);
			popup.setOnHiding(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					TitledPane pane = (TitledPane) popup.getOwnerNode();
					VBox container = map.get(pane);
					List<Node> lstNode = new ArrayList<Node>(infoBox.getChildren());
					for (Node node : lstNode) {
						if (node != infoName && !(node instanceof Label)) {
							container.getChildren().add(node);
						}
					}
				}
			});

			popup.setOnAutoHide(new EventHandler<Event>() {
				@Override
				public void handle(Event event) {
					armed.set(false);
				}
			});

		}
		return popup;
	}

	private void prepareContent(TitledPane pane) {
		VBox container = map.get(pane);
		infoBox.getChildren().addAll(container.getChildren());
	}

	public void addMenuItems(List<SideMenuItem> lstMenuItem) {
		for (SideMenuItem menuItem : lstMenuItem) {
			recursiveBuild(expMenuContainer, menuItem);
		}
	}
	
	public void addMenuItems(SideMenuItem... lstMenuItem) {
		for (SideMenuItem menuItem : lstMenuItem) {
			recursiveBuild(expMenuContainer, menuItem);
		}
	}

	private void recursiveBuild(Pane container, SideMenuItem menu) {
		if (!(menu instanceof SideMenuCategory)) {
			if(menu.isPane()){
				
				VBox pane = new VBox();
				pane.setMaxWidth(Double.MAX_VALUE);
				pane.setAlignment(Pos.CENTER);
				pane.getStyleClass().add("side-menu-button");
				
//				pane.setPrefHeight(200);
				
				try {
					Node node = (Node) Class.forName(menu.getFrmClass()).newInstance();
					pane.getChildren().add(node);
				} catch (InstantiationException | IllegalAccessException
						| ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				container.setPadding(new Insets(-5));
				container.getChildren().add(pane);
				
			}else{
				Button btn = new Button(menu.getLabel());
				ImageView imageView = new ImageView();
				imageView.getStyleClass().add(menu.getImageStyle());
				btn.setGraphic(imageView);
				btn.setUserData(menu);
				btn.setOnAction(eventHandler);
				btn.setCursor(Cursor.HAND);
				btn.getStyleClass().add("side-menu-button");
				btn.setMaxWidth(Double.MAX_VALUE);
				btn.setAlignment(Pos.CENTER_LEFT);
				container.getChildren().add(btn);
			}
		} else {
			final TitledPane pane = new TitledPane();
			pane.setText(menu.getLabel());
			VBox content = new VBox();
			content.setPadding(new Insets(0, 0, 0, 20));
			content.setSpacing(2);
			pane.setContent(content);
			for (SideMenuItem child : ((SideMenuCategory) menu).getMenuItems()) {
				recursiveBuild(content, child);
			}
			pane.setExpanded(false);

			map.put(pane, content);
			ImageView imageView = new ImageView();
			imageView.getStyleClass().add(menu.getImageStyle());
			pane.setGraphic(imageView);
			pane.setUserData(menu);
			pane.setContent(content);
			pane.setExpanded(false);
//			pane.lookup(".arrow").setVisible(false);
			pane.expandedProperty().addListener(new ChangeListener<Boolean>() {
				
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
					if (!toggleExpand) {
						return;
					}
					toggleExpand = false;
					Set<Node> panes = pane.getParent().lookupAll(".titled-pane");
					for (Node node : panes) {
						if (node instanceof TitledPane) {
							TitledPane titledPane = (TitledPane) node;
							if (titledPane.isExpanded() && titledPane != pane) {
								titledPane.setExpanded(false);
								break;
							}
						}
					}
					toggleExpand = true;
				}
			});
			pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (!expanded.get()) {
						if (popup == null || !popup.isShowing()) {
							TitledPane pane = (TitledPane) event.getSource();
							armed.set(true);
							showPopupMenu(pane);
						} else {
							popup.hide();
							armed.set(false);
						}
					}
				}
			});
			pane.setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (armed.get()) {
						if (popup.isShowing()) {
							popup.hide();
						}
						TitledPane pane = (TitledPane) event.getSource();
						showPopupMenu(pane);
					}
				}
			});


			container.getChildren().add(pane);
		}
	}

	private void showPopupMenu(TitledPane pane) {
		Scene scene = pane.getScene();
		final Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
		final Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
		final Point2D nodeCoord = pane.localToScene(0.0, 0.0);
		final double clickX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());
		final double clickY = Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY());
		Popup p = getPopup();
		infoName.setText(pane.getText());
		prepareContent(pane);
		p.show(pane, clickX + pane.getWidth(), clickY);
	}
	private EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			if (popup != null && popup.isShowing()) {
				popup.hide();
				armed.set(false);
			}
			Button btn = (Button) event.getSource();
			SideMenuItem mainMenuItem = (SideMenuItem) btn.getUserData();
			facade.executeAction(mainMenuItem.getActionName());
		}
	};

}
