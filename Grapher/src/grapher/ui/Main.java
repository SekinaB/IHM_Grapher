package grapher.ui;

import java.util.List;

import grapher.fc.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.ColorPicker;
import javafx.scene.Scene;
//import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Main extends Application {
	public void start(Stage stage) {
		BorderPane root = new BorderPane();
		TableView<FunctionC> table = new TableView<FunctionC>();
		TableColumn<FunctionC, String> functionColumn = new TableColumn<FunctionC, String>("Function"); 
		TableColumn<FunctionC, ColorPicker> colorColumn = new TableColumn<FunctionC, ColorPicker>("Couleur"); ; 
		//List<String> listfun = ;//{"sin(x)", "tan(x)", "x"};//
		for(int i = 0; i<getParameters().getRaw().size();i++){
			FunctionC f = new FunctionC(FunctionFactory.createFunction(getParameters().getRaw().get(i)), Color.BLACK);
			table.getItems().add(f);
		}
		
		functionColumn.setCellValueFactory(param -> { 
		    final Function function = param.getValue().getFunction(); 
		    return new SimpleStringProperty(function.toString()); 
		}); 
		colorColumn.setCellValueFactory(param -> { 
		    final ColorPicker color = param.getValue().getColor(); 
		    return new SimpleObjectProperty<>(color); 
		}); 

		table.getColumns().setAll(functionColumn, colorColumn);

//		colorColumn.setEditable(true);
		
//		colorColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<FunctionC>() {   
//		    @Override 
//		    public String toString(FunctionC object) { 
//		        return object.getFunction().toString(); 
//		    } 
//		  
//		    @Override 
//		    public FunctionC fromString(String string) { 
//		        return new FunctionC(FunctionFactory.createFunction(string), Color.BLACK); 
//		    } 
//		}));
		
		ToolBar tools = new ToolBar();
		MenuBar menuBar = new MenuBar(new Menu("Expression"));
		BorderPane left = new BorderPane();
		root.setCenter(new SplitPane(left, new GrapherCanvas(getParameters().getRaw(), table, tools, menuBar)));
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