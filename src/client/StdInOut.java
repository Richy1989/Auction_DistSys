package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Richard Leopold
 */
public class StdInOut implements Runnable
{

    private Logger logger = new Logger(StdInOut.class);
    private Client client;
    private boolean run = true;
    private BufferedReader bufferdReader;
    private InputStreamReader streamReader;
    private InputStream stream;
    private String name;

    public StdInOut(Client client)
    {
        stream = System.in;
        streamReader = new InputStreamReader(stream);
        bufferdReader = new BufferedReader(streamReader);
        this.client = client;
        this.name = "";
    }

    @Override
    public void run()
    {
        logger.debug("StdInput Reader started");
        System.out.print("\n" + this.name + "> ");
        String message = "";

        while (run)
        {
            try
            {
                if (bufferdReader.ready())
                {
                    message = bufferdReader.readLine();
                    logger.debug("Std input: Mesage receaved: " + message);
                    client.sendMessage(message);
                }
                Thread.sleep(200);
            }
            catch (InterruptedException ex)
            {
                logger.debug("StdInOut - Wait ex");
            }
            catch (IOException ex)
            {
                run = false;
                logger.debug("StdInOut - BufferReader Read Closed");
            }
        }
    }

    public void stopWork()
    {
        this.run = false;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public void newMessage(String message)
    {
        System.out.println(message);
        System.out.print(this.name + "> ");
    }
}