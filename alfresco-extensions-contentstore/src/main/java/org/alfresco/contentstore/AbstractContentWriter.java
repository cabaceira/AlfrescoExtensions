/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.contentstore;

import org.sglover.alfrescoextensions.common.Node;

/**
 * 
 * @author sglover
 *
 */
public abstract class AbstractContentWriter implements ContentWriter
{
    protected Node node;

    public AbstractContentWriter(Node node)
    {
        this.node = node;
    }

//    @Override
//    public ContentReference getContentReference()
//    {
//        return node;
//    }
}
