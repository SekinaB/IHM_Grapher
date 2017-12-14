package grapher.ui;

import grapher.fc.*;

import grapher.fc.Function;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.Scene;
import javafx.scene.control.ListView;

public class Main extends Application {
	public void start(Stage stage) {
		BorderPane root = new BorderPane();
		ListView<Function> list = new ListView<Function>();
		ToolBar tools = new ToolBar();
		MenuBar menuBar = new MenuBar(new Menu("Expression"));
		for(String e : getParameters().getRaw()){
			list.getItems().add(FunctionFactory.createFunction(e));
		}
		BorderPane left = new BorderPane();
		root.setCenter(new SplitPane(left, new GrapherCanvas(list, tools, menuBar)));
		left.setTop(menuBar);
		left.setCenter(list);
		left.setBottom(tools);
		stage.setTitle("grapher");
		stage.setScene(new Scene(root));
		stage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}