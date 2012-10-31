/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 *
 * @author Richy
 */
public class Client implements IReadMessage
{

    private Logger logger = new Logger(Client.class);
    private int corePoolSize = 5;
    private ScheduledThreadPoolExecutor threadPool;// = new ScheduledThreadPoolExecutor(corePoolSize);//, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
    private Socket socket = null;
    private TCPReader tcpRead;
    private UDPReader udpRead;
    private Sender send;
    private StdInOut stdInOut;
    private String host;
    private int udpPort;
    private int tcpPort;
    private String name;

    public Client(String host, int tcpPort, int udpPort)
    {
        this.host = host;
        this.udpPort = udpPort;
        this.tcpPort = tcpPort;
    }

    public void connect()
    {
        boolean socketOK;
        try
        {
            socket = new Socket(host, this.tcpPort); // verbindet sich mit Server
            socketOK = true;
        }
        catch (IOException ex)
        {
            logger.debug("Problem building socket");
            socketOK = false;
        }

        if (socketOK)
        {
            threadPool = new ScheduledThreadPoolExecutor(corePoolSize);
            tcpRead = new TCPReader(socket, this);
            udpRead = new UDPReader(this.udpPort, this);
            send = new Sender(socket);
            stdInOut = new StdInOut(this);
            //addTask(send);
            addTask(udpRead);
            addTask(tcpRead);
            addTask(stdInOut);
            udpRead.addNewMessageEvent(this);
            tcpRead.addNewMessageEvent(this);
            sendMessage("!udpport " + udpPort);
        }
    }

    public void disconnect()
    {

        logger.debug("Client Stop Sender");
        send.stopWork();

        logger.debug("Client - Disconnect");
        try
        {
            socket.close();
        }
        catch (IOException ex)
        {
            logger.debug("Problem closing socket");
        }


        logger.debug("Client Stop UDP Reader");
        udpRead.stopWork();

        logger.debug("Client Stop TCP Reader");
        tcpRead.stopWork();
        logger.debug("Client Stop std in out");
        stdInOut.stopWork();
        logger.debug("Client shut down threadpool");
        threadPool.shutdown();


        udpRead = null;
        send = null;
        tcpRead = null;
        stdInOut = null;
    }

    public synchronized void exitClient()
    {
        // System.exit(0);
        disconnect();
    }

    public void addTask(Runnable task)
    {
        threadPool.execute(task);
    }

    public void sendMessage(String message)
    {
        String[] input = message.split(" ");

        if (input[0].equals("!login"))
        {
            if (input.length >= 2)
            {
                message = "";
                for (int i = 1; i < input.length; i++)
                {
                    if (i < input.length - 1)
                    {
                        message += input[i] + "_";
                    }
                    else
                    {
                        message += input[i];
                    }
                }
                if (stdInOut.getName().equals(""))
                {
                    this.name = message;
                    stdInOut.setName(this.name);
                
                }

                message = input[0] + " " + message;
            }
            else
            {
                return;
            }
        }

        logger.debug("Client -- Send Message: " + message);
        send.send(message);
        addTask(send);
    }

    @Override
    public synchronized void newMessage(String message)
    {
        if (message != null)
        {
            String[] input = message.split(" ");

            if (input[0].equals("!logout"))
            {
                disconnect();
                connect();
                return;
            }

            if (input[0].equals("!end"))
            {
                disconnect();
                //System.exit(0);
                return;
            }

            if (input[0].equals("!udp-ok"))
            {
                logger.debug("Upd Port OK Connection initialized");
                return;
            }

            if (input[0].equals("!new-bid"))
            {
                if (input.length >= 2)
                {
                    String aucName = "";
                    for (int i = 1; i < input.length; i++)
                    {
                        aucName += input[i];
                    }
                    stdInOut.newMessage("You have been overbid on '" + aucName + "'");
                    return;
                }
                return;
            }

            if (input[0].equals("!user-twice"))
            {
                if (input.length >= 2)
                {
                    stdInOut.setName("");
                    stdInOut.newMessage("User '" + input[1] + "' already logged in");
                    return;
                }
                return;
            }

            if (input[0].equals("!auction-ended"))
            {
                if (input.length >= 4)
                {
                    String winnerName = input[1];

                    if (winnerName.equals(this.name))
                    {
                        winnerName = "You";
                    }

                    String aucName = "";
                    for (int i = 3; i < input.length; i++)
                    {
                        aucName += input[i];
                    }

                    stdInOut.newMessage("The auction " + aucName + " has ended. " + winnerName + " won with " + input[2] + ".");
                }
                return;
            }
            stdInOut.newMessage(message);
        }
    }
}