package server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Richard Leopold
 */
public class Server
{

    private Logger logger = new Logger(Server.class);
    private HashMap<Integer, Auction> auctions;
    private int corePoolSize = 10;
    private List<User> users;
    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(corePoolSize);
    private int auctionCount = 0;
    private StdInputReader reader;
    private SocketListener socketListener;
    private int port;

    public Server(int port)
    {
        this.port = port;
        users = new ArrayList<User>();
        auctions = new HashMap<Integer, Auction>();
    }

    public void start()
    {
        socketListener = new SocketListener(this, port);
        reader = new StdInputReader(this);
        addTask(reader);
        addTask(socketListener);
    }

    public synchronized void addTask(Runnable task)
    {
        //logger.debug("New work in ThreadPool, actual size: " + workQueue.size());
        threadPool.execute(task);
    }

    public synchronized Auction addNewAuction(User owner, String description, Date endDate)
    {
        logger.debug("Add new Auction, id: " + auctionCount);
        Auction a = new Auction(auctionCount, description, owner, endDate, this);
        auctions.put(auctionCount, a);
        auctionCount++;
        return a;
    }

    public void removeUser(User user)
    {
        synchronized (users)
        {
            if (user != null)
            {
                if (users.contains(user))
                {
                    users.remove(user);
                }
            }
        }
    }

    public synchronized void stopRunningUser(User user)
    {
        if (user != null)
        {
            threadPool.remove(user);
        }
    }

    public void addUser(User user)
    {
        synchronized (users)
        {
            users.add(user);
        }
        addTask(user);
    }

    public synchronized void removeAuction(Auction auction)
    {
        if (auction != null)
        {
            if (auctions.containsKey(auction.getID()))
            {
                auctions.remove(auction.getID());
            }
        }
    }

    public List<User> getUserList()
    {
        return this.users;
    }

    public Auction getAuction(int auctionID)
    {
        if (auctions.containsKey(auctionID))
        {
            return auctions.get(auctionID);
        }
        return null;
    }

    public void endServer()
    {
        logger.debug("Server - End Auctions");
        for (Auction a : auctions.values())
        {
            a.stopTimer();
        }

        logger.debug("Server - End Users");
        for (User u : users)
        {
            logger.debug("!end user: " + u.getName());
            u.logout("!end");
        }


        logger.debug("Server - End reader");
        reader.stop();
        logger.debug("Server - End Socker Listener");
        socketListener.stopWork();
        logger.debug("Server - Shut Down Thread pool");
        threadPool.shutdown();
    }

    public boolean addUDPPort(int udp)
    {
        synchronized (users)
        {
            for (User u : users)
            {
                if (u.getUDPPort() == udp)
                {
                    return false;
                }
            }
        }

        return true;

    }

    public HashMap<Integer, Auction> getAllAuctions()
    {
        return this.auctions;
    }
}
