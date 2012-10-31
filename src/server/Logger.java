/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

//import org.apache.log4j.PropertyConfigurator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;

/**
 *
 * @author Richy
 */
public class Logger
{

    private Class clazz;
    //private static org.apache.log4j.Logger logger;

    public Logger(Class clazz)
    {
        this.clazz = clazz;
        //PropertyConfigurator.configureAndWatch("log4j-3.properties", 60 * 1000);
        //logger = org.apache.log4j.Logger.getLogger(clazz);
    }

    public void debug(String debugMessage)
    {
        Date now = new Date();
        //System.out.println(now.toString() + " - " + clazz.getName() + " - " + debugMessage);
    }
}
