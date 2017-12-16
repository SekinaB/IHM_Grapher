package grapher.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.ColorPicker;
import javafx.scene.Scene;

import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import javafx.scene.control.cell.TextFieldTableCell;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Main extends Application {
	public void start(Stage stage) {
		BorderPane root = new BorderPane();

		/*
		 * Creation of the tool bar for the buttons.
		 */
		ToolBar tools = new ToolBar();

		/*
		 * Creation of the Menu.
		 */
		MenuBar menuBar = new MenuBar(new Menu("Expression"));

		/*
		 * Creation of the TableView as a table of FunctionC.
		 */
		TableView<FunctionC> table = new TableView<FunctionC>();
		TableColumn<FunctionC, String> functionColumn = new TableColumn<FunctionC, String>("Function");
		TableColumn<FunctionC, ColorPicker> colorColumn = new TableColumn<FunctionC, ColorPicker>("Couleur");

		/*
		 * Filling of the TableView with the functions and their default color,
		 * Black.
		 */
		for (String e : getParameters().getRaw()) {
			FunctionC f = new FunctionC(e, Color.BLACK);
			table.getItems().add(f);
		}
		functionColumn.setCellValueFactory(param -> {
			return new SimpleStringProperty(param.getValue().getFunction());
		});
		colorColumn.setCellValueFactory(param -> {
			return new SimpleObjectProperty<>(param.getValue().getColor());
		});
		table.getColumns().setAll(functionColumn, colorColumn);

		/*
		 * Creation of the grapher.
		 */
		GrapherCanvas grapher = new GrapherCanvas(getParameters().getRaw(), table, tools, menuBar);

		/*
		 * Allowing the edition of the functions' column.
		 */
		table.setEditable(true);
		functionColumn.setCellFactory(TextFieldTableCell.forTableColumn());

		/**
		 * Management of the event 'Edition' 
		 * @copyright Servan & Zoran. Zoran gave us the idea to use an
		 *            'updating' function from GrapherCanvas
		 */
		functionColumn.setOnEditCommit((TableColumn.CellEditEvent<FunctionC, String> t) -> {
			grapher.newFunction(((CellEditEvent<FunctionC, String>) t).getNewValue(),
					((CellEditEvent<FunctionC, String>) t).getOldValue(),
					((CellEditEvent<FunctionC, String>) t).getTablePosition().getRow());
		});
		
		/*
		 * Management of the display
		 */
		BorderPane left = new BorderPane();
		root.setCenter(new SplitPane(left, grapher));
		left.setTop(menuBar);
		left.setCenter(table);
		left.setBottom(tools);
		stage.setTitle("grapher");
		stage.setScene(new Scene(root));
		stage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}