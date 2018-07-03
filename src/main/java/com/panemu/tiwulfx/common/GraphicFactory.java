/*
 * Copyright (C) 2015 Panemu.
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
package com.panemu.tiwulfx.common;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * This class responsible to create image/graphic to various TiwulFX Control.
 * Developer can extends this class and overrides the methods to create custom
 * images. Call {@link TiwulFXUtil#setGraphicFactory(GraphicFactory)} to replace
 * the default factory.
 *
 * @author amrullah
 */
public class GraphicFactory {

	private static final Image imgAdd = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/add.png"));
	private static final Image imgDelete = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/delete.png"));
	private static final Image imgEdit = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/edit.png"));
	private static final Image imgExport = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/export.png"));
	private static final Image imgReload = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/reload.png"));
	private static final Image imgSave = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/save.png"));
	private static final Image imgFilter = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/filter.png"));
	private static final Image imgLookup = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/lookup.png"));

	private static final Image imgConfirm = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/confirm48.png"));
	private static final Image imgError = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/error48.png"));
	private static final Image imgInfo = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/info48.png"));
	private static final Image imgWarning = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/warning48.png"));

	private static final Image imgMenuCollapse = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/left.png"));
	private static final Image imgMenuExpand = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/right.png"));

	private static final Image imgRequired = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/required.png"));
	private static final Image imgInvalid = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/invalid.png"));
	private static final Image imgRequiredInvalid = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/required_invalid.png"));

	private static final Image imgConfig = new Image(GraphicFactory.class.getResourceAsStream("/com/panemu/tiwulfx/res/images/conf.png"));

	/**
	 * Graphic of Add button in TableControl
	 *
	 * @return
	 */
	public Node createAddGraphic() {
		return new ImageView(imgAdd);
	}

	/**
	 * Graphic of Delete button in TableControl
	 *
	 * @return
	 */
	public Node createDeleteGraphic() {
		return new ImageView(imgDelete);
	}

	/**
	 * Graphic of Edit button in TableControl
	 *
	 * @return
	 */
	public Node createEditGraphic() {
		return new ImageView(imgEdit);
	}

	/**
	 * Graphic of Export button in TableContrl
	 *
	 * @return
	 */
	public Node createExportGraphic() {
		return new ImageView(imgExport);
	}

	/**
	 * Graphic of Reload button in TableControl
	 *
	 * @return
	 */
	public Node createReloadGraphic() {
		return new ImageView(imgReload);
	}

	/**
	 * Graphic of Save button in TableControl
	 *
	 * @return
	 */
	public Node createSaveGraphic() {
		return new ImageView(imgSave);
	}

	/**
	 * Graphic of go-to-first-page button in TableControl
	 *
	 * @return
	 */
	public Node createPageFirstGraphic() {
		Region rg = new Region();
		rg.getStyleClass().addAll("pagination-arrow", "first-shape");
		return rg;
	}

	/**
	 * Graphic of go-to-previous-page button in TableControl
	 *
	 * @return
	 */
	public Node createPagePrevGraphic() {
		Region rg = new Region();
		rg.getStyleClass().addAll("pagination-arrow", "prev-shape");
		return rg;
	}

	/**
	 * Graphic of go-to-next-page button in TableControl
	 *
	 * @return
	 */
	public Node createPageNextGraphic() {
		Region rg = new Region();
		rg.getStyleClass().addAll("pagination-arrow", "next-shape");
		return rg;
	}

	/**
	 * Graphic of go-to-last-page button in TableControl
	 *
	 * @return
	 */
	public Node createPageLastGraphic() {
		Region rg = new Region();
		rg.getStyleClass().addAll("pagination-arrow", "last-shape");
		return rg;
	}

	/**
	 * Graphic of Filter button in TableControl
	 *
	 * @return
	 */
	public Node createFilterGraphic() {
		ImageView filterImage = new ImageView(imgFilter);
		filterImage.setFitHeight(20.0);
		filterImage.setFitWidth(20.0);
		return filterImage;
	}

	/**
	 * Graphic of Lookup button in LookupField or LookupColumn
	 *
	 * @return
	 */
	public Node createLookupGraphic() {
		return new ImageView(imgLookup);
	}

	/**
	 * Image of Confirmation dialog
	 *
	 * @return
	 */
	public Image getDialogConfirmImage() {
		return imgConfirm;
	}

	/**
	 * Image of Error dialog
	 *
	 * @return
	 */
	public Image getDialogErrorImage() {
		return imgError;
	}

	/**
	 * Image of Information dialog
	 *
	 * @return
	 */
	public Image getDialogInfoImage() {
		return imgInfo;
	}

	/**
	 * Image of Warning dialog
	 *
	 * @return
	 */
	public Image getDialogWarningImage() {
		return imgWarning;
	}

	/**
	 * Graphic of collapse button in sidemenu
	 *
	 * @return
	 */
	public Node createMenuCollapseGraphic() {
		return new ImageView(imgMenuCollapse);
	}

	/**
	 * Graphic of expand button in sidemenu
	 *
	 * @return
	 */
	public Node createMenuExpandGraphic() {
		return new ImageView(imgMenuExpand);
	}

	/**
	 * Image located next to a control indicating it's value is required. The
	 * default is a red asterisk.
	 *
	 * @return
	 */
	public Image getValidationRequiredImage() {
		return imgRequired;
	}

	/**
	 * Image located next to a control indicating it's value is invalid. The
	 * default is a red exclamation mark
	 *
	 * @return
	 */
	public Image getValidationInvalidImage() {
		return imgInvalid;
	}

	/**
	 * Image located next to a control indicating it's value is required but
	 * invalid. The default is a red asterisk and exclamation mark.
	 *
	 * @return
	 */
	public Image getValidationRequiredInvalidImage() {
		return imgRequiredInvalid;
	}

	/**
	 * Graphic of a configuration button in the footer of TableControl. The
	 * default is a small gear
	 *
	 * @return
	 */
	public Node createConfigGraphic() {
		return new ImageView(imgConfig);
	}
}
