/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author Richard Leopold
 */
public class Main
{

    private static Logger logger = new Logger(Auction.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        boolean ok;
        int port = 0;

        if (args.length == 1)
        {
            logger.debug("Server program start");
            try
            {
                port = Integer.parseInt(args[0]);
                ok = true;
            }
            catch (IllegalArgumentException ex)
            {
                logger.debug("Wrong arguments -- can not convert to Integer: " + ex.getMessage());
                System.out.println("Wrong arguments\nUSAGE: <TCP Port>");
                ok = false;
            }

            if (ok)
            {
                Server server = new Server(port);
                server.start();
            }
        }
        else
        {
            logger.debug("Wrong arguments -- program end");
            System.out.println("Wrong arguments\nUSAGE: <TCP Port>");
        }
    }
}
