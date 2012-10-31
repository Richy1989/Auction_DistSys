/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.EventListener;

/**
 *
 * @author Richy
 */
public interface IReadMessage extends EventListener
{
	void newMessage(String message);
}
