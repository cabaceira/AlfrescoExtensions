/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.cacheserver.events;

import java.io.Serializable;

import org.sglover.checksum.NodeChecksums;

/**
 * 
 * @author sglover
 *
 */
public class ChecksumsAvailableEvent implements Serializable
{
	private static final long serialVersionUID = 6707502577332049969L;

	private String cacheServerId;
	private String contentUrl;
	private NodeChecksums checksums;

	public ChecksumsAvailableEvent()
	{
	}

	public ChecksumsAvailableEvent(String cacheServerId, String contentUrl, NodeChecksums checksums)
    {
	    super();
	    this.cacheServerId = cacheServerId;
	    this.contentUrl = contentUrl;
	    this.checksums = checksums;
    }

	public String getCacheServerId()
	{
		return cacheServerId;
	}

	public void setCacheServerId(String cacheServerId)
	{
		this.cacheServerId = cacheServerId;
	}

	public String getContentUrl()
	{
		return contentUrl;
	}

	public NodeChecksums getChecksums()
	{
		return checksums;
	}

	public void setChecksums(NodeChecksums checksums)
	{
		this.checksums = checksums;
	}

	@Override
    public String toString()
    {
	    return "ChecksumsAvailableEvent [cacheServerId=" + cacheServerId
	            + ", contentUrl=" + contentUrl + ", checksums=" + checksums + "]";
    }


}
