/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Richy
 */
public class SocketListener implements Runnable
{

    private Logger logger = new Logger(SocketListener.class);
    private Server server;
    private ServerSocket serverSocket;
    private int port = 0;
    private boolean run = true;

    public SocketListener(Server server, int port)
    {
        this.port = port;
        this.server = server;

        try
        {
            logger.debug("Socket Listener init Socket: " + port);
            serverSocket = new ServerSocket(port);
        }
        catch (IOException ex)
        {
            logger.debug("Could not listen on port: " + port);
            server.endServer();///System.out.println("Could not listen on port: " + port);
        }
    }

    public void waitNewUser()
    {
        logger.debug("Wait for new User");
        Socket clientSocket = null;

        try
        {
            clientSocket = serverSocket.accept();
        }
        catch (IOException e)
        {
            logger.debug("Problem waiting for new user: " + e.getMessage());
            this.run = false;
            //System.out.println("Accept failed");
        }

        if (clientSocket != null)
        {
            logger.debug("New user connected - Start work");
            User user = new User(clientSocket, server);
            server.addUser(user);
        }
    }

    @Override
    public void run()
    {
        while (run)
        {
            waitNewUser();
        }
    }
    
    public void stopWork()
    {
        logger.debug("SocketListener -- Start Close");
        
        try
        {
            this.serverSocket.close();
        }
        catch (IOException ex)
        {
            logger.debug("Socket Listener -- Problem Closing Server Socket");
        }
        this.run = false;
        logger.debug("SocketListener -- Finished Close");
    }
    
}
