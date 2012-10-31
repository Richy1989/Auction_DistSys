/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Richard Leopold
 */
public class Auction extends TimerTask
{

    private Logger logger = new Logger(Auction.class);
    private String description;
    private User owner;
    private Date endDate;
    private User highestBidUser = null;
    private double bid = 0.0;
    private int id = -1;
    private Server server;
    private boolean run = true;
    private Timer timer;

    public Auction(int id, String description, User owner, Date endDate, Server server)
    {
        this.timer = new Timer();

        timer.schedule(this, endDate);

        this.id = id;
        this.endDate = endDate;
        this.description = description;
        this.owner = owner;
        this.server = server;
    }

    public int getID()
    {
        return this.id;
    }

    public void setID(int id)
    {
        if (this.id == -1)
        {
            this.id = id;
        }
    }

    public synchronized Auction setNewBid(User user, double bid)
    {
        if (bid > this.bid)
        {
            this.bid = bid;

            if (highestBidUser != null && !(highestBidUser.getName().equals(owner.getName())))
            {
                highestBidUser.setUDPMessage("!new-bid " + this.description);
                highestBidUser.removeAuction(this);
            }

            this.highestBidUser = user;
            this.highestBidUser.setTCPMessage("You successfully bid with " + this.bid + " on '" + this.description + "'");
            return this;
        }
        user.setTCPMessage("You unsuccessfully bid with " + bid + " on '" + this.description + "'. Current highest bid is " + this.bid + ".");
        return null;
    }

    public double getNewBid()
    {
        return this.bid;
    }

    public Date getEndDate()
    {
        return this.endDate;
    }

    public String getDescription()
    {
        return this.description;
    }

    public User getHighestBidUser()
    {
        return this.highestBidUser;
    }

    public String getActionMessage()
    {
        String actualBidder = "none";
        if (highestBidUser != null)
        {
            actualBidder = highestBidUser.getName();
        }
        return id + " " + description + " " + owner.getName() + " " + endDate.toString() + " " + bid + " " + actualBidder;
    }

    public String getName()
    {
        return this.description;
    }

    @Override
    public void run()
    {
        logger.debug("Auction finished");

        String highestBidUserName = "none";
        if (highestBidUser != null)
        {
            highestBidUserName = highestBidUser.getName();
            highestBidUser.setUDPMessage("!auction-ended " + highestBidUserName + " " + this.bid + " " + this.description);
            highestBidUser.removeAuction(this);
        }

        owner.setUDPMessage("!auction-ended " + highestBidUserName + " " + this.bid + " " + this.description);
        owner.removeAuction(this);
        server.removeAuction(this);
        timer.cancel();
    }

    public void stopTimer()
    {
        if (highestBidUser != null)
        {
            highestBidUser.removeAuction(this);
        }
        owner.removeAuction(this);
        logger.debug("Auction -- Canceling Timer");
        timer.cancel();
        logger.debug("Auction -- Timer Canceled");
    }
}
