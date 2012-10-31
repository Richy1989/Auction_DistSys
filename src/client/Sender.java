/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author Richy
 */
public class Sender implements Runnable
{

	private Logger logger = new Logger(Sender.class);
	private Socket socket;
	private PrintStream printWriter;
	private String message = null;
	boolean run = true;

	public Sender(Socket socket)
	{
		this.socket = socket;
		try
		{
			printWriter = new PrintStream(socket.getOutputStream());
		}
		catch (IOException ex)
		{
			logger.debug("Sender -- Problem setting Printstream");
		}
	}

	@Override
	public void run()
	{
		if (this.message != null && run)
		{
			printWriter.print(this.message);
			this.message = null;
		}
	}
        
        public void stopWork()
        {
            this.run = false;
        }

	public synchronized void send(String message)
	{
		logger.debug("Sender -- send command receaved Message: " + message);
		this.message = message;
	}
}
