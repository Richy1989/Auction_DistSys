package client;

/**
 * @author Richard Leopold
 */
public class Main
{

    public static void main(String[] args)
    {
        boolean argsOK = false;
        int tcpPort = 12350;
        int udpPort = 12353;
        String host = "localhost";

        //Client c = new Client(host, tcpPort, udpPort);
        //c.connect();
        if (args.length >= 3)
        {
            try
            {
                host = args[0];
                tcpPort = Integer.parseInt(args[1]);
                udpPort = Integer.parseInt(args[2]);
                argsOK = true;
               
            }
            catch (IllegalArgumentException ex)
            {
                System.out.print("USAGE: <host> <TCP Port> <UDP Port>");
                argsOK = false;
            }

            if (argsOK)
            {
                Client c = new Client(host, tcpPort, udpPort);
                c.connect();
            }
        }
        else
        {
            System.out.print("USAGE: <host> <TCP Port> <UDP Port>");
        }
   }
}