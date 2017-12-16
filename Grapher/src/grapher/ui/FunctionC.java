package grapher.ui;

import grapher.fc.Function;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class FunctionC {
	private Function function;
	private ColorPicker color = new ColorPicker();

	public FunctionC(Function f, Color c) {
		this.function = f;
		this.color.setValue(c);
	}

	public Function getFunction() {
		return this.function;
	}

	public ColorPicker getColor() {
		return this.color;
	}
}
