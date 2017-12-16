package grapher.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
/**
 * This is a class use to combine a function (String) and a color (ColorPicker).
 */
public class FunctionC {
	private String function;
	private ColorPicker color = new ColorPicker();

	/**
	 * Class constructor 
	 * @param f a function in a string form
	 * @param c color of the function on the graph
	 */
	public FunctionC(String f, Color c) {
		this.function = f;
		this.color.setValue(c);
	}

	/**
	 * Getter of function 
	 * @return function
	 */
	public String getFunction() {
		return this.function;
	}
	
	/**
	 * Getter of color 
	 * @return color 
	 */
	public ColorPicker getColor() {
		return this.color;
	}
	
	/**
	 * Setter of function 
	 * @param f
	 */
	public void setFunction(String f) {
		this.function = f;
	}

	/**
	 * Setter of function 
	 * @param c 
	 */
	public void setColor(Color c) {
		this.color.setValue(c);;
	}
}
