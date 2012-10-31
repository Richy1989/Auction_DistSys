/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Richy
 */
public class UDPReader implements Runnable
{

    private Logger logger = new Logger(UDPReader.class);
    private EventListenerList eventList = new EventListenerList();
    private DatagramSocket dsocket = null;
    private DatagramPacket packet = null;
    private int udpPort = 0;
    private boolean run = true;
    private Client client;

    public UDPReader(int udpPort, Client client)
    {
        this.client = client;
        this.udpPort = udpPort;
    }

    @Override
    public void run()
    {
        while (run)
        {
            String message = getUDPMessage();
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

    public String getUDPMessage()
    {
        byte[] buffer = new byte[1024];

        try
        {
            dsocket = new DatagramSocket(this.udpPort);
            packet = new DatagramPacket(buffer, buffer.length);
            this.dsocket.receive(packet);

            dsocket.close();
            return new String(buffer, 0, packet.getLength());
        }
        catch (IOException ex)
        {
            logger.debug("UDPReader - IO Exception in receave UDP Message: " + ex.getMessage());
            this.run = false;
            //client.sendMessage("!end");
        }

        return null;
    }

    public void stopWork()
    {
        this.run = false;
        logger.debug("Start - Closing Socket");

        if (dsocket == null)
        {
            logger.debug("Socket is null");
            return;
        }

        if (dsocket.getLocalPort() != -1)
        {
            logger.debug("Closing Socket");
            dsocket.close();
        }
    }

    public void addNewMessageEvent(IReadMessage messageEvent)
    {
        logger.debug("UDPReader - Add new Event in Eventlistener List");
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
