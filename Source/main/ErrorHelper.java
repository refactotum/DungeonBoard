package main;

import javax.swing.*;

public class ErrorHelper
{
	public static ErrorHelper Instance = new ErrorHelper(Main.Instance);

	private Main _main;

	public ErrorHelper(Main main)
	{
		this._main = main;
	}

	public void showError(String message)
	{
		JOptionPane.showMessageDialog(_main.getControl(), message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void showError(String message, Throwable error)
	{
		JOptionPane.showMessageDialog(_main.getControl(), message + "\n" + error.getMessage());
	}
}
