/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore.dao;

/**
 * 
 * @author sglover
 *
 */
public class UserContext
{
    private static ThreadLocal<String> user = new ThreadLocal<>();

    public static String getUser()
    {
        return user.get();
    }

    public static void setUser(String user)
    {
        UserContext.user.set(user);
    }

    public static void clearUser()
    {
        UserContext.user.set(null);
    }
}
