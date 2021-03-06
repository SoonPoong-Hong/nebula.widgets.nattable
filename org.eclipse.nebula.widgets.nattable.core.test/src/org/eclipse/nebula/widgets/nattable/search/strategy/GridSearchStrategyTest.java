/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.strategy;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GridSearchStrategyTest {

    // Has 10 columns and 5 rows
    private DefaultGridLayer gridLayer;
    private ConfigRegistry configRegistry;

    @Before
    public void setUp() {
        this.gridLayer = new DefaultGridLayer(getBodyDataProvider(),
                GridLayerFixture.colHeaderDataProvider);
        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display
                .getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));

        this.configRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration()
                .configureRegistry(this.configRegistry);
    }

    public IDataProvider getBodyDataProvider() {
        return new IDataProvider() {
            final IDataProvider bodyDataProvider = GridLayerFixture.bodyDataProvider;

            @Override
            public int getColumnCount() {
                return this.bodyDataProvider.getColumnCount();
            }

            @Override
            public Object getDataValue(int columnIndex, int rowIndex) {
                Object dataValue = null;
                if (columnIndex == 2 && rowIndex == 2) {
                    dataValue = "body";
                } else if (columnIndex == 4 && rowIndex == 4) {
                    dataValue = "Body";
                } else if (columnIndex == 3 && rowIndex == 3) {
                    dataValue = "Body";
                } else if (columnIndex == 0 && rowIndex == 0) {
                    dataValue = "Body";
                } else {
                    dataValue = this.bodyDataProvider.getDataValue(columnIndex,
                            rowIndex);
                }
                return dataValue;
            }

            @Override
            public int getRowCount() {
                return this.bodyDataProvider.getRowCount();
            }

            @Override
            public void setDataValue(int columnIndex, int rowIndex,
                    Object newValue) {
                this.bodyDataProvider.setDataValue(columnIndex, rowIndex, newValue);
            }

        };
    }

    @Test
    public void searchShouldWrapAroundColumn() {
        // Select search starting point in composite coordinates
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 3, 4, false, false));

        GridSearchStrategy gridStrategy = new GridSearchStrategy(
                this.configRegistry, false, true);

        // If we don't specify to wrap the search, it will not find it.
        final SelectionLayer selectionLayer = this.gridLayer.getBodyLayer()
                .getSelectionLayer();
        gridStrategy.setContextLayer(selectionLayer);
        gridStrategy.setCaseSensitive(true);
        gridStrategy
                .setComparator(new CellValueAsStringComparator<Comparable<String>>());
        Assert.assertNull(gridStrategy.executeSearch("body"));

        gridStrategy.setWrapSearch(true);
        // Should find it when wrap search is enabled.
        Assert.assertNotNull(gridStrategy.executeSearch("Body"));
    }

    @Test
    public void searchShouldWrapAroundRow() {
        // Select search starting point in composite coordinates
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 3, 4, false, false));

        GridSearchStrategy gridStrategy = new GridSearchStrategy(
                this.configRegistry, false, true);
        gridStrategy
                .setComparator(new CellValueAsStringComparator<Comparable<String>>());
        // If we don't specify to wrap the search, it will not find it.
        final SelectionLayer selectionLayer = this.gridLayer.getBodyLayer()
                .getSelectionLayer();
        gridStrategy.setContextLayer(selectionLayer);
        Assert.assertNull(gridStrategy.executeSearch("[1,3]"));

        gridStrategy.setWrapSearch(true);

        // Should find it when wrap search is enabled.
        Assert.assertNotNull(gridStrategy.executeSearch("[1,3]"));
    }

    @Test
    public void searchShouldMoveBackwardsToFindCell() {
        // Select search starting point in composite coordinates
        this.gridLayer
                .doCommand(new SelectCellCommand(this.gridLayer, 3, 4, false, false));

        GridSearchStrategy gridStrategy = new GridSearchStrategy(
                this.configRegistry, false, ISearchDirection.SEARCH_BACKWARDS, true);
        gridStrategy
                .setComparator(new CellValueAsStringComparator<Comparable<String>>());
        final SelectionLayer selectionLayer = this.gridLayer.getBodyLayer()
                .getSelectionLayer();
        gridStrategy.setContextLayer(selectionLayer);

        Assert.assertNotNull(gridStrategy.executeSearch("[1,3]"));
    }

    @Test
    public void shouldFindAllCellsWithValue() {
        GridSearchStrategy gridStrategy = new GridSearchStrategy(
                this.configRegistry, true, ISearchDirection.SEARCH_BACKWARDS, true);
        gridStrategy
                .setComparator(new CellValueAsStringComparator<Comparable<String>>());
        final SelectionLayer selectionLayer = this.gridLayer.getBodyLayer()
                .getSelectionLayer();
        gridStrategy.setContextLayer(selectionLayer);
        gridStrategy.setCaseSensitive(true);

        PositionCoordinate searchResult = gridStrategy.executeSearch("Body");
        Assert.assertEquals(0, searchResult.columnPosition);
        Assert.assertEquals(0, searchResult.rowPosition);

        gridStrategy.setWrapSearch(true);
        // Simulate selecting the search result
        selectionLayer.doCommand(new SelectCellCommand(selectionLayer,
                searchResult.columnPosition, searchResult.rowPosition, false,
                false));
        searchResult = gridStrategy.executeSearch("Body");
        // System.out.println(searchResult);
        selectionLayer.doCommand(new SelectCellCommand(selectionLayer,
                searchResult.columnPosition, searchResult.rowPosition, false,
                false));
        searchResult = gridStrategy.executeSearch("Body");
        // System.out.println(searchResult);
        Assert.assertEquals(3, searchResult.columnPosition);
        Assert.assertEquals(3, searchResult.rowPosition);
    }
}
