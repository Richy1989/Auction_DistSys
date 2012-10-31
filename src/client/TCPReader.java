package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Richard Leopold
 */
public class TCPReader implements Runnable
{

    private Logger logger = new Logger(TCPReader.class);
    private EventListenerList eventList = new EventListenerList();
    private List<String> messages;
    private Socket socket;
    private DatagramSocket dsocket = null;
    private BufferedReader bufferedReader;
    private Client client;
    private boolean run = true;

    public TCPReader(Socket socket, Client client)
    {
        this.client = client;

        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException ex)
        {
            logger.debug("TCP Reader - Problem Creating BufferReader");
        }

        this.messages = new ArrayList<String>();
        this.socket = socket;
    }

    @Override
    public void run()
    {
        while (run)
        {
            String message = getTCPMessage();
            if (message != null)
            {
                String[] input = message.split("#~");
                for (int i = 0; i < input.length; i++)
                {
                    fireNewMessageEvent(input[i]);
                }
            }
        }
    }

    public String getActualMessage()
    {
        if (this.messages.size() > 0)
        {
            String message = messages.get(0);
            messages.remove(0);
            return message;
        }
        return null;
    }

    public String getTCPMessage()
    {
        char[] buffer = new char[200];
        buffer[0] = (char) -1;
        int signCount = 0;
        try
        {
            signCount = bufferedReader.read(buffer); // blockiert bis Nachricht empfangen

            if (signCount == -1)
            {
                logger.debug("TCPReader --  -1 length receaved... close all");
                this.run = false;
                //client.disconnect();
                //client.exitClient();
            }
            String message = new String(buffer, 0, signCount);
            logger.debug("TCPReader -- message received: " + message);
            return message;
        }
        catch (IOException ex)
        {
            logger.debug("TCPReader - BufferedReader Read Cancles");
            this.run = false;
        }

        return null;
    }

    public void stopWork()
    {
        try
        {
            socket.close();
        }
        catch (IOException ex)
        {
            logger.debug("TCP Reader - Stop Work, Socket Close Problem");
        }
        this.run = false;
    }

    public void addNewMessageEvent(IReadMessage messageEvent)
    {
        eventList.add(IReadMessage.class, messageEvent);
    }

    public void fireNewMessageEvent(String messgae)
    {
        for (IReadMessage r : eventList.getListeners(IReadMessage.class))
        {
            r.newMessage(messgae);
        }
    }
}