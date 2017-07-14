/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WavFileProcessor;

/**
 *
 * @author HP
 */
public class WavFileException extends Exception
{
	public WavFileException()
	{
		super();
	}

	public WavFileException(String message)
	{
		super(message);
	}

	public WavFileException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WavFileException(Throwable cause) 
	{
		super(cause);
	}
}

