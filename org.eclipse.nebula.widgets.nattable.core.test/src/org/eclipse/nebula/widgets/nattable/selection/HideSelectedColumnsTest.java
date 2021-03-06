/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectColumnCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HideSelectedColumnsTest {
    private SelectionLayer selectionLayer;
    private ColumnHideShowLayer columnHideShowLayer;

    @Before
    public void setUp() {
        this.columnHideShowLayer = new ColumnHideShowLayer(new DataLayerFixture());
        this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
    }

    @Test
    public void shouldAlsoHideColumnWhichIsNotSelectedButHasAMouseOverIt() {
        this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2));
        Assert.assertTrue(this.columnHideShowLayer.isColumnIndexHidden(2));
    }

    @Test
    public void shouldHideColumnForSelectedCell() {
        // Select cell in column we want to hide
        this.selectionLayer.setSelectedCell(3, 0);

        // Hide selection
        this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 3));

        // The previously selected column should be hidden
        Assert.assertTrue(this.columnHideShowLayer.isColumnIndexHidden(3));
        Assert.assertEquals(4, this.selectionLayer.getColumnCount());
    }

    @Test
    public void shouldHideSelectedColumn() {
        // Select column to hide
        new SelectColumnCommandHandler(this.selectionLayer).selectColumn(2, 0,
                false, false);

        // Hide column
        this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2));

        // The previously selected column should be hidden
        Assert.assertTrue(this.columnHideShowLayer.isColumnIndexHidden(2));
        Assert.assertEquals(4, this.selectionLayer.getColumnCount());
    }

    @Test
    public void shouldHideAllSelectedColumns() {
        // Select cells and columns
        new SelectColumnCommandHandler(this.selectionLayer).selectColumn(2, 0,
                false, true);
        this.selectionLayer.selectCell(1, 0, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);

        // Hide selection
        this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer,
                new int[] { 2, 0, 4 }));

        // Previously selected columns should be hidden
        Assert.assertTrue(this.columnHideShowLayer.isColumnIndexHidden(2));
        Assert.assertTrue(this.columnHideShowLayer.isColumnIndexHidden(0));
        Assert.assertTrue(this.columnHideShowLayer.isColumnIndexHidden(4));
    }
}
