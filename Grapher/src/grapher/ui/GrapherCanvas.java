package grapher.ui;

import static java.lang.Math.*;

import java.util.List;
import java.util.Optional;

import javafx.util.converter.DoubleStringConverter;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import grapher.fc.*;

public class GrapherCanvas extends Canvas {
	static final double MARGIN = 40;
	static final double STEP = 5;

	static final double WIDTH = 400;
	static final double HEIGHT = 300;

	private static DoubleStringConverter d2s = new DoubleStringConverter();

	protected double W = WIDTH;
	protected double H = HEIGHT;

	protected double xmin, xmax;
	protected double ymin, ymax;

	protected Rectangle2D r;

	protected TableView<FunctionC> tableFunctions = new TableView<FunctionC>();
	protected ListView<Function> listFunctions = new ListView<Function>();

	public GrapherCanvas(List<String> listfun, TableView<FunctionC> table, ToolBar tools, MenuBar menuBar) {
		super(WIDTH, HEIGHT);
		xmin = -PI / 2.;
		xmax = 3 * PI / 2;
		ymin = -1.5;
		ymax = 1.5;

		// Management of the event linked to the mouse
		this.addEventHandler(MouseEvent.ANY, new HandlerMouse());

		// Management of the event linked to the scroll on the mouse
		this.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			Point2D p;

			@Override
			public void handle(ScrollEvent event) {
				p = new Point2D(event.getX(), event.getY());
				zoom(p, event.getDeltaY());
			}
		});

		tableFunctions = table;

		// Initialization of the list of function, used to ease their usage.
		for (String e : listfun) {
			listFunctions.getItems().add(FunctionFactory.createFunction(e));
		}

		// Management of the selection of item in the table 
		tableFunctions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FunctionC>() {
			@Override
			public void changed(ObservableValue<? extends FunctionC> observable, FunctionC oldValue,
					FunctionC newValue) {
				redraw();
			}
		});

		// Creation of the button
		Button add = new Button("Add");
		Button del = new Button("Delete");
		Button clr = new Button("Clear");

		// Setting of their action
		add.setOnAction(new HandlerAdd());
		del.setOnAction(new HandlerDel());
		clr.setOnAction(new HandlerClr());

		// Addition of the buttons in the tool box
		tools.getItems().addAll(add, del, clr);

		// Creation of the lines in the menu
		MenuItem addM = new MenuItem("Add new function");
		MenuItem delM = new MenuItem("Delete a function");
		MenuItem clrM = new MenuItem("Clear");

		// Setting of their action
		addM.setOnAction(new HandlerAdd());
		delM.setOnAction(new HandlerDel());
		clrM.setOnAction(new HandlerClr());

		// Addition of their accelerators
		addM.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		delM.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
		clrM.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

		// Addition of the lines in the menu
		menuBar.getMenus().get(0).getItems().addAll(addM, delM, clrM);
	}

	public double minHeight(double width) {
		return HEIGHT;
	}

	public double maxHeight(double width) {
		return Double.MAX_VALUE;
	}

	public double minWidth(double height) {
		return WIDTH;
	}

	public double maxWidth(double height) {
		return Double.MAX_VALUE;
	}

	public boolean isResizable() {
		return true;
	}

	public void resize(double width, double height) {
		super.setWidth(width);
		super.setHeight(height);
		redraw();
	}

	public void redraw() {
		GraphicsContext gc = getGraphicsContext2D();

		W = getWidth();
		H = getHeight();

		// background
		gc.clearRect(0, 0, W, H);

		gc.setFill(Color.BLACK);
		gc.setStroke(Color.BLACK);

		// box
		gc.save();
		gc.translate(MARGIN, MARGIN);
		W -= 2 * MARGIN;
		H -= 2 * MARGIN;
		if (W < 0 || H < 0) {
			return;
		}

		gc.strokeRect(0, 0, W, H);

		gc.fillText("x", W, H + 10);
		gc.fillText("y", -10, 0);

		gc.beginPath();
		gc.rect(0, 0, W, H);
		gc.closePath();
		gc.clip();

		// plot
		gc.translate(-MARGIN, -MARGIN);

		// x values
		final int N = (int) (W / STEP + 1);
		final double dx = dx(STEP);
		double xs[] = new double[N];
		double Xs[] = new double[N];
		for (int i = 0; i < N; i++) {
			double x = xmin + i * dx;
			xs[i] = x;
			Xs[i] = X(x);
		}

		for (int j = 0; j < listFunctions.getItems().size(); j++) {
			// y values
			double Ys[] = new double[N];
			for (int i = 0; i < N; i++) {
				Ys[i] = Y(listFunctions.getItems().get(j).y(xs[i]));
			}
			// If the function is selected in the table
			if (tableFunctions.getSelectionModel().isSelected(j)) {
				// it is drawn bold
				gc.setLineWidth(2.75);
			} else {
				gc.setLineWidth(1);
			}
			// The function is draw according to its color
			gc.setStroke(Paint.valueOf(tableFunctions.getItems().get(j).getColor().getValue().toString()));
			gc.strokePolyline(Xs, Ys, N);

		}

		gc.restore(); // restoring no clipping

		// axes
		drawXTick(gc, 0);
		drawYTick(gc, 0);

		double xstep = unit((xmax - xmin) / 10);
		double ystep = unit((ymax - ymin) / 10);

		gc.setLineDashes(new double[] { 4.f, 4.f });
		for (double x = xstep; x < xmax; x += xstep) {
			drawXTick(gc, x);
		}
		for (double x = -xstep; x > xmin; x -= xstep) {
			drawXTick(gc, x);
		}
		for (double y = ystep; y < ymax; y += ystep) {
			drawYTick(gc, y);
		}
		for (double y = -ystep; y > ymin; y -= ystep) {
			drawYTick(gc, y);
		}
		
		// If the rectangle exist, it is draw
		if (r != null)
			gc.strokeRect(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());

		gc.setLineDashes(null);

	}

	protected double dx(double dX) {
		return (double) ((xmax - xmin) * dX / W);
	}

	protected double dy(double dY) {
		return -(double) ((ymax - ymin) * dY / H);
	}

	protected double x(double X) {
		return xmin + dx(X - MARGIN);
	}

	protected double y(double Y) {
		return ymin + dy((Y - MARGIN) - H);
	}

	protected double X(double x) {
		double Xs = (x - xmin) / (xmax - xmin) * W;
		return Xs + MARGIN;
	}

	protected double Y(double y) {
		double Ys = (y - ymin) / (ymax - ymin) * H;
		return (H - Ys) + MARGIN;
	}

	protected void drawXTick(GraphicsContext gc, double x) {
		if (x > xmin && x < xmax) {
			final double X0 = X(x);
			gc.strokeLine(X0, MARGIN, X0, H + MARGIN);
			gc.fillText(d2s.toString(x), X0, H + MARGIN + 15);
		}
	}

	protected void drawYTick(GraphicsContext gc, double y) {
		if (y > ymin && y < ymax) {
			final double Y0 = Y(y);
			gc.strokeLine(0 + MARGIN, Y0, W + MARGIN, Y0);
			gc.fillText(d2s.toString(y), 5, Y0);
		}
	}

	protected static double unit(double w) {
		double scale = pow(10, floor(log10(w)));
		w /= scale;
		if (w < 2) {
			w = 2;
		} else if (w < 5) {
			w = 5;
		} else {
			w = 10;
		}
		return w * scale;
	}

	protected void translate(double dX, double dY) {
		double dx = dx(dX);
		double dy = dy(dY);
		xmin -= dx;
		xmax -= dx;
		ymin -= dy;
		ymax -= dy;
		redraw();
	}

	protected void zoom(Point2D center, double dz) {
		double x = x(center.getX());
		double y = y(center.getY());
		double ds = exp(dz * .01);
		xmin = x + (xmin - x) / ds;
		xmax = x + (xmax - x) / ds;
		ymin = y + (ymin - y) / ds;
		ymax = y + (ymax - y) / ds;
		redraw();
	}

	protected void zoom(Point2D p0, Point2D p1) {
		double x0 = x(p0.getX());
		double y0 = y(p0.getY());
		double x1 = x(p1.getX());
		double y1 = y(p1.getY());
		xmin = min(x0, x1);
		xmax = max(x0, x1);
		ymin = min(y0, y1);
		ymax = max(y0, y1);
		redraw();
	}

	public class HandlerMouse implements EventHandler<MouseEvent> {
		State state = State.IDLE;
		Point2D p;

		public void handle(MouseEvent event) {
			switch (state) {
			case IDLE:
				switch (event.getEventType().getName()) {
				case "MOUSE_PRESSED":
					p = new Point2D(event.getX(), event.getY());
					state = State.PRESSED;
					break;
				default:
				}
				break;
			case PRESSED:
				switch (event.getButton().name()) {
				case "PRIMARY":
					switch (event.getEventType().getName()) {
					case "MOUSE_RELEASED":
						zoom(p, 5);
						state = State.IDLE;
						break;
					case "MOUSE_DRAGGED":
						p = new Point2D(event.getX(), event.getY());
						state = State.LEFT_DRAGGED;
						break;
					default:
					}
					break;
				case "SECONDARY":
					switch (event.getEventType().getName()) {
					case "MOUSE_RELEASED":
						zoom(p, -5);
						state = State.IDLE;
						break;
					case "MOUSE_DRAGGED":
						p = new Point2D(event.getX(), event.getY());
						state = State.RIGHT_DRAGGED;
						break;
					default:
					}
					break;
				default:
				}
				break;
			case LEFT_DRAGGED:
				switch (event.getEventType().getName()) {
				case "MOUSE_DRAGGED":
					translate(event.getX() - p.getX(), event.getY() - p.getY());
					p = new Point2D(event.getX(), event.getY());
					break;
				case "MOUSE_RELEASED":
					translate(event.getX() - p.getX(), event.getY() - p.getY());
					state = State.IDLE;
					break;
				default:
				}
				break;
			case RIGHT_DRAGGED:
				switch (event.getEventType().getName()) {
				case "MOUSE_DRAGGED":
					Point2D p_min = new Point2D(min(p.getX(), event.getX()), min(p.getY(), event.getY()));
					double height = abs(event.getY() - p.getY());
					double width = abs(event.getX() - p.getX());
					r = new Rectangle2D(p_min.getX(), p_min.getY(), width, height);
					redraw();
					break;
				case "MOUSE_RELEASED":
					zoom(new Point2D(r.getMinX(), r.getMinY()), new Point2D(r.getMaxX(), r.getMaxY()));
					r = null;
					redraw();
					state = State.IDLE;
					break;
				default:
				}
				break;
			default:
			}
		}
	}

	public class HandlerAdd implements EventHandler<ActionEvent> {
		public void handle(ActionEvent arg0) {
			String result = userInput();
			try {
				listFunctions.getItems().add(FunctionFactory.createFunction(result));
				tableFunctions.getItems().add(new FunctionC(result, Color.BLACK));
				redraw();
			} catch (Exception e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("ERROR");
				alert.setHeaderText("Function not found");
				alert.setContentText("Error function : Couldn't evaluate function");
				alert.showAndWait();
			}
		}

		public String userInput() {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Add a new function");
			dialog.setHeaderText("What fonction would you like to add?");
			Optional<String> s = dialog.showAndWait();
			if (s.isPresent()) {
				return s.get();
			}
			return null;
		}
	}

	public class HandlerDel implements EventHandler<ActionEvent> {
		Alert alert = new Alert(AlertType.ERROR);

		@Override
		public void handle(ActionEvent event) {
			if (tableFunctions.getSelectionModel().isEmpty()) {
				alert.setTitle("ERROR");
				alert.setHeaderText("No function selected");
				alert.setContentText("Error function : Couldn't delete the function");
				alert.showAndWait();
			} else {
				int indfunction = tableFunctions.getSelectionModel().getSelectedIndex();
				tableFunctions.getItems().remove(indfunction);
				listFunctions.getItems().remove(indfunction);
				redraw();
			}
		}
	}

	public class HandlerClr implements EventHandler<ActionEvent> {
		Alert alert = new Alert(AlertType.ERROR);

		@Override
		public void handle(ActionEvent event) {
			tableFunctions.getItems().clear();
			listFunctions.getItems().clear();
			redraw();
		}
	}

	/**
	 * @copyright Servan & Zoran Zoran gave us the idea to implement this
	 *            function
	 */
	public void newFunction(String newF, String oldF, int ind) {
		try {
			listFunctions.getItems().set(ind, FunctionFactory.createFunction(newF));
			tableFunctions.getItems().get(ind).setFunction(newF);
			redraw();
		} catch (Exception e) {
			TableColumn.editCancelEvent();
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Function not found");
			alert.setContentText("Error function : Couldn't evaluate function");
			alert.showAndWait();
			;
		}
	}

}