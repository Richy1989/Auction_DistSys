package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Richard Leopold
 */
public class User implements Runnable
{

    private Logger logger = new Logger(User.class);
    private String name = null;
    private Socket clientSocket;
    private List<String> actualTCPMessages;
    private List<String> actualUDPMessages;
    private boolean isAnonymus = true;
    private Server server;
    private List<Auction> actualAuctions;
    private boolean run = true;
    private boolean isLoggedOut = true;
    private int udpPort = -1;
    private BufferedReader bufferedReader;
    private InputStreamReader inputStreamReader;
    private PrintStream printStream = null;
    private boolean isUDPSet = false;

    public User(Socket clientSocket, Server server)
    {
        logger.debug("Creat new user");
        actualTCPMessages = new ArrayList<String>();
        actualUDPMessages = new ArrayList<String>();
        actualAuctions = new ArrayList<Auction>();
        this.clientSocket = clientSocket;
        this.server = server;
        initBuffer();
    }

    @Override
    public void run()
    {
        logger.debug(name + " user work started");
        String[] inputLine;
        boolean commandFound;
        boolean loginFirst = false;
        while (run)
        {
            commandFound = false;
            String input = receive();

            if (input != null)
            {
                inputLine = input.split(" ");

                if (inputLine.length != 0)
                {
                    if (inputLine[0].equals("!udpport") && inputLine.length > 1)
                    {
                        commandFound = true;
                        logger.debug("UDPPORT command receaved");
                        if (!isUDPSet)
                        {
                            try
                            {
                                int udp = Integer.parseInt(inputLine[1]);
                                if (server.addUDPPort(udp))
                                {
                                    isUDPSet = true;
                                    setUDPPort(udp);
                                }
                                else
                                {
                                    logger.debug("UDPPORT already in USE -> logout");
                                    logout("!end");
                                }
                            }
                            catch (IllegalArgumentException ex)
                            {
                                logger.debug("User -- Problem converting udpPort");
                            }
                        }
                        else
                        {
                             logger.debug("UDPPORT command twice");
                        }
                    }

                    if (inputLine[0].equals("!login") && inputLine.length > 1)
                    {
                        commandFound = true;
                        logger.debug("Login command receaved");

                        checkUserExist(inputLine[1]);
                        if (!this.isLoggedOut)
                        {
                            sendPendingMessages();
                        }
                    }

                    if (inputLine[0].equals("!list"))
                    {
                        commandFound = true;
                        logger.debug("List command receaved");
                        sendAllRunningAuctions();
                    }

                    if (inputLine[0].equals("!logout"))
                    {
                        commandFound = true;
                        logger.debug("Logout command receaved");
                        if (!isAnonymus)
                        {
                            logout("!logout");
                        }
                        else
                        {
                            setTCPMessage("You have to login first");
                        }
                    }

                    if (inputLine[0].equals("!end"))
                    {
                        commandFound = true;
                        logger.debug("End command receaved");
                        logout("!end");//server.endServer();
                    }

                    if (!commandFound)
                    {
                        if (!isLoggedOut && !isAnonymus)
                        {

                            if (inputLine[0].equals("!create") && inputLine.length > 2)
                            {
                                boolean ok;
                                commandFound = true;
                                logger.debug("Create command receaved");

                                int duration = 0;
                                String desciption = "";

                                try
                                {
                                    duration = Integer.parseInt(inputLine[1]);
                                    ok = true;
                                }
                                catch (IllegalArgumentException ex)
                                {
                                    setTCPMessage("Wrong input");
                                    logger.debug("Failure create new Auction");
                                    ok = false;
                                }

                                if (ok)
                                {
                                    for (int i = 2; i < inputLine.length; i++)
                                    {
                                        desciption += inputLine[i] + " ";
                                    }
                                    desciption = desciption.trim();

                                    Date end = new Date();
                                    end.setTime(end.getTime() + duration * 1000);

                                    addNewAuction(desciption, end);
                                }

                            }

                            if (inputLine[0].equals("!bid") && inputLine.length > 2)
                            {
                                commandFound = true;
                                boolean ok;
                                logger.debug("Bid command received");

                                int auctionID = 0;
                                int amount = 0;
                                try
                                {
                                    auctionID = Integer.parseInt(inputLine[1]);
                                    amount = Integer.parseInt(inputLine[2]);
                                    ok = true;
                                }
                                catch (IllegalArgumentException ex)
                                {
                                    setTCPMessage("Wrong input");
                                    logger.debug("Problem during convert auctionID or amount to Int");
                                    ok = false;
                                }
                                if (ok)
                                {
                                    setNewBid(auctionID, amount);
                                }
                            }
                        }
                    }
                }

                if (loginFirst)
                {
                    logger.debug("Login first");
                    setTCPMessage("You have to ligin first");
                }
                else if (commandFound == false)
                {
                    logger.debug("Command not found");
                    setTCPMessage("Commnd: '" + inputLine[0] + "' not found");
                }
                input = null;
            }
        }
    }

    private void sendTCP(String message)
    {
        logger.debug("Send TCP Message: " + message);
        printStream.print(message);
    }

    private void sendUDP(String message)
    {
        logger.debug("User -- Send UDP Message: " + message);
        byte[] byteMessage = message.getBytes();

        InetAddress address;

        try
        {
            address = clientSocket.getInetAddress();

            DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, address, this.udpPort);

            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();
        }
        catch (UnknownHostException ex)
        {
            logger.debug("User -- UnknownHostException: " + ex.getMessage());
        }
        catch (SocketException ex)
        {
            logger.debug("User -- SocketException: " + ex.getMessage());
        }
        catch (IOException ex)
        {
            logger.debug("User -- IOException: " + ex.getMessage());
            //logout("!end");
        }
    }

    public void setTCPMessage(String message)
    {
        message = message + "#~";
        if (!isLoggedOut || isAnonymus)
        {
            sendTCP(message);
        }
        else
        {
            actualTCPMessages.add(message);
        }
    }

    public void setUDPMessage(String message)
    {
        message = message + "#~";
        if (!isLoggedOut || isAnonymus)
        {
            sendUDP(message);
        }
        else
        {
            actualUDPMessages.add(message);
        }
    }

    private void sendPendingMessages()
    {
        while (!actualTCPMessages.isEmpty())
        {
            String message = actualTCPMessages.get(0);
            actualTCPMessages.remove(0);
            sendTCP(message);
        }

        while (!actualUDPMessages.isEmpty())
        {
            String message = actualUDPMessages.get(0);
            actualUDPMessages.remove(0);
            sendUDP(message);
        }
    }

    private String receive()
    {
        char[] buffer = new char[512];
        String input = null;
        int length = 0;
        try
        {
            //input = in.readLine();
            length = bufferedReader.read(buffer);//input.length();

            if (length == -1)
            {
                logger.debug("User -- Recieve -- Length -1");
                this.run = false;//  logout("!logout");
                return null;
            }

            input = new String(buffer, 0, length);
            logger.debug("User -- Message receaved: " + input);
            return input;
        }
        catch (IOException ex)
        {
            logger.debug(name + ": IO Exception while recieve - " + ex.getMessage());
            this.run = false; //logout("!logout");
        }
        return null;
    }

    public void addNewAuction(String description, Date endDate)
    {
        Auction addedAuction = server.addNewAuction(this, description, endDate);

        if (addedAuction != null)
        {
            setTCPMessage("An auction '" + addedAuction.getDescription() + "' with id " + addedAuction.getID() + " has been created and will end on " + addedAuction.getEndDate());
            actualAuctions.add(addedAuction);
        }
    }

    public void setNewBid(int auctionID, double amount)
    {
        Auction auction = server.getAuction(auctionID);
        if (auction != null)
        {
            Auction bidedAuction = auction.setNewBid(this, amount);
            if (bidedAuction != null)
            {
                if (!actualAuctions.contains(bidedAuction))
                {
                    actualAuctions.add(bidedAuction);
                }
            }
        }
        else
        {
            setTCPMessage("No Auction with ID: " + auctionID);
            logger.debug("No auction with ID: " + auctionID);
        }
    }

    public void setIsLoggedOut(boolean isLoggedOut)
    {
        this.isLoggedOut = isLoggedOut;
    }

    public void setIsAnonymus(boolean isAnonymus)
    {
        this.isAnonymus = isAnonymus;
    }

    public String getName()
    {
        if (this.name == null)
        {
            return "null";
        }
        return this.name;
    }

    public void setSocketClient(Socket socket)
    {
        this.clientSocket = socket;
    }

    public void logout(String logoutMessage)
    {
        logger.debug("Logout User: " + this.name + " with logoutmessage: " + logoutMessage);
        setTCPMessage("Successfully logged out as " + this.name + "!");
        setTCPMessage(logoutMessage);

        this.udpPort = -1;
        if (actualAuctions.isEmpty() && !logoutMessage.equals("!end"))
        {
            server.removeUser(this);
        }

        this.isLoggedOut = true;
        this.run = false;

        try
        {
            Thread.sleep(200);
            //bufferedReader.close();
            clientSocket.close();
            logger.debug("User - Logout: Socket Closed");
        }
        catch (InterruptedException ex)
        {
            logger.debug("User - Logout - InterruptedException: " + ex.getMessage());
        }
        catch (IOException ex)
        {
            logger.debug("User - Logout - IOException: " + ex.getMessage());
        }
    }

    private void sendAllRunningAuctions()
    {
        logger.debug("Read Auctions --> send to client");

        Collection<Auction> runningAuctions = server.getAllAuctions().values();

        if (runningAuctions.size() > 0)
        {
            for (Auction ac : runningAuctions)
            {
                setTCPMessage(ac.getActionMessage());
            }
        }
        else
        {
            setTCPMessage("No Auctions runnning");
        }

    }

    private void checkUserExist(String name)
    {
        if (isAnonymus)
        {
            logger.debug("Check if user: " + name + " exists");
            for (User u : server.getUserList())
            {
                if (u.getName() != null)
                {
                    if (u.getName().equals(name))
                    {
                        logger.debug("User: " + name + " found, reactivate");

                        if (u.isLoggedOut)
                        {
                            u.setSocketClient(this.clientSocket);
                            u.setUDPPort(this.udpPort);
                            u.setName(name);
                            u.setTCPMessage("Successfully logged in as " + name);
                            u.restart();

                            this.run = false;
                            this.isLoggedOut = true;
                            server.removeUser(this);

                            server.removeUser(u);
                            server.addUser(u);
                        }
                        else
                        {
                            // setTCPMessage("User: " + name + " already loggen in.");
                            setTCPMessage("!user-twice " + name);
                        }

                        return;
                    }
                }
            }

            logger.debug("No user foud -- Create New: " + name);
            this.name = name;
            isAnonymus = false;
            isLoggedOut = false;
            setTCPMessage("Successfully logged in as " + this.name);
        }
        else
        {
            setTCPMessage("You are loggen in as " + this.name + " - logout first");
        }
    }

    public boolean isLoggedOut()
    {
        return this.isLoggedOut;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void restart()
    {
        this.run = true;
        this.isLoggedOut = false;
        this.isAnonymus = false;

        initBuffer();
        sendPendingMessages();
    }

    public void initBuffer()
    {
        try
        {
            inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            printStream = new PrintStream(clientSocket.getOutputStream());
        }
        catch (IOException ex)
        {
            logger.debug("User -- Problem init new BufferReader");
        }
    }

    public void setUDPPort(int port)
    {
        this.udpPort = port;
        setUDPMessage("!udp-ok");
    }

    public int getUDPPort()
    {
        return this.udpPort;
    }

    public void removeAuction(Auction auction)
    {
        if (actualAuctions.contains(auction))
        {
            actualAuctions.remove(auction);
        }
    }
}