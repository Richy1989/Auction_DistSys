/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Richy
 */
public class StdInputReader implements Runnable
{

    private Logger logger = new Logger(StdInputReader.class);
    private BufferedReader bufferdReader;
    private boolean run = true;
    private Server server;

    public StdInputReader(Server server)
    {
        this.server = server;
        bufferdReader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run()
    {
        while (run)
        {
            char[] buffer = new char[1024];
            int length = 0;
            String message = "";
            try
            {
                if (bufferdReader.ready())
                {
                    length = bufferdReader.read(buffer);//readLine();

                    if (length == -1)
                    {
                        this.run = false;
                    }

                    message = new String(buffer);
                    logger.debug("Std input: Mesage receaved: " + message);
                    server.endServer();
                }
                else
                {
                    Thread.sleep(200);
                }
            }
            catch (IOException ex)
            {
                this.run = false;
                logger.debug("StdInOut - BufferReader Read Closed");
            }
            catch (InterruptedException ex)
            {
                this.run = false;
                logger.debug("StdInOut - Wait Problem");
            }
        }
        logger.debug("StdInOut - Finished");
    }

    public void stop()
    {
        this.run = false;
    }
}