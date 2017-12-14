package grapher.ui;

import static java.lang.Math.*;

import java.util.Optional;

import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
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

	protected ListView<Function> listFunctions;

	public GrapherCanvas(ListView<Function> list, ToolBar tools, MenuBar menuBar) {
		super(WIDTH, HEIGHT);
		xmin = -PI / 2.;
		xmax = 3 * PI / 2;
		ymin = -1.5;
		ymax = 1.5;
		// Gestion des evenements lies au clique de la souris
		this.addEventHandler(MouseEvent.ANY, new HandlerMouse());

		// Gestion des evenements lies a la molette de la souris
		this.addEventHandler(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			Point2D p;

			@Override
			public void handle(ScrollEvent event) {
				p = new Point2D(event.getX(), event.getY());
				zoom(p, event.getDeltaY());
			}
		});

		list.setEditable(true);
		listFunctions = list;

		// Gestion des selections de fonction dans la liste
		listFunctions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Function>() {
			@Override
			public void changed(ObservableValue<? extends Function> observable, Function oldValue, Function newValue) {
				redraw();
			}
		});

		listFunctions.setCellFactory(new Callback<ListView<Function>, ListCell<Function>>() {
			@Override
			public ListCell<Function> call(ListView<Function> param) {
				ListCell<Function> new_cell = new ListCell<Function>(){
					
				};
				return new_cell;
			}
		});

		// Creation des boutons
		Button add = new Button("Add");
		Button del = new Button("Delete");
		Button clr = new Button("Clear");

		// Gestion de l'action des boutons
		add.setOnAction(new HandlerAdd());
		del.setOnAction(new HandlerDel());
		clr.setOnAction(new HandlerClr());

		// Ajout des boutons dans la boite a outil
		tools.getItems().addAll(add, del, clr);

		// Creation des lignes du menu
		MenuItem addM = new MenuItem("Add new function");
		MenuItem delM = new MenuItem("Delete a function");
		MenuItem clrM = new MenuItem("Clear");

		// Gestion de l'action des ligne
		addM.setOnAction(new HandlerAdd());
		delM.setOnAction(new HandlerDel());
		clrM.setOnAction(new HandlerClr());

		// Ajout des raccourcis clavie
		addM.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		delM.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
		clrM.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

		// Ajout des ligne dans le menu
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

	private void redraw() {
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
			if (listFunctions.getSelectionModel().isSelected(j)) {
				gc.setLineWidth(2.75);
				gc.strokePolyline(Xs, Ys, N);
			} else {
				gc.setLineWidth(1);
				gc.strokePolyline(Xs, Ys, N);
			}

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
		// TODO : Code pas optimal demander pour le dessin d'un rectangle
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
			// System.out.println(event.getButton().name() + " " +
			// event.getEventType().getName());
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
					// TODO : Code pas optimal demander pour le dessin d'un
					// rectangle
					redraw();
					break;
				case "MOUSE_RELEASED":
					// TODO : Code pas optimal demander pour le dessin d'un
					// rectangle
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
			if (listFunctions.getSelectionModel().isEmpty()) {
				alert.setTitle("ERROR");
				alert.setHeaderText("No function selected");
				alert.setContentText("Error function : Couldn't delete the function");
				alert.showAndWait();
			}
			Function function = listFunctions.getSelectionModel().getSelectedItem();
			if (!listFunctions.getItems().remove(function)) {
				alert.setTitle("ERROR");
				alert.setHeaderText("Function not found");
				alert.setContentText("Error function : Couldn't delete function");
				alert.showAndWait();
			}
			redraw();
		}
	}

	public class HandlerClr implements EventHandler<ActionEvent> {
		Alert alert = new Alert(AlertType.ERROR);

		@Override
		public void handle(ActionEvent event) {
			if (listFunctions.getItems().isEmpty()) {
				alert.setTitle("ERROR");
				alert.setHeaderText("No function in the list");
				alert.setContentText("Error function : Couldn't delete the functions");
				alert.showAndWait();
			}
			listFunctions.getItems().clear();
			redraw();
		}
	}
}