/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.entities.EntitiesService;
import org.alfresco.entities.EntitiesServiceImpl;
import org.alfresco.entities.EntitiesServiceImpl.ExtracterType;
import org.alfresco.entities.UserTrackingService;
import org.alfresco.entities.dao.mongo.MongoEntitiesDAO;
import org.alfresco.entities.dao.mongo.MongoEventsDAO;
import org.alfresco.entities.dao.mongo.MongoSimilarityDAO;
import org.alfresco.events.node.types.NodeAddedEvent;
import org.alfresco.events.node.types.NodeContentGetEvent;
import org.alfresco.events.node.types.NodeContentPutEvent;
import org.alfresco.events.node.types.NodeRemovedEvent;
import org.alfresco.events.node.types.NodeUpdatedEvent;
import org.alfresco.events.node.types.TransactionCommittedEvent;
import org.alfresco.events.serialize.EventSerializer;
import org.alfresco.events.serialize.EventsSerializer;
import org.alfresco.events.serialize.NodeAddedEventSerializer;
import org.alfresco.events.serialize.NodeContentPutEventSerializer;
import org.alfresco.events.serialize.NodeRemovedEventSerializer;
import org.alfresco.events.serialize.NodeUpdatedEventSerializer;
import org.alfresco.extensions.common.Node;
import org.alfresco.services.nlp.DefaultModelLoader;
import org.alfresco.services.nlp.ModelLoader;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author sglover
 *
 */
public class SimilarityTest extends AbstractEntitiesTest
{
	private static Log logger = LogFactory.getLog(SimilarityTest.class.getName());

	private MockContentGetter contentGetter;
	private EntitiesService entitiesService;

	private MongoEventsDAO eventsDAO;
//	private EventsListener eventsListener;

	@Before
	public void before() throws Exception
	{
		super.init();
		this.contentGetter = MockContentGetter.start();
		ModelLoader modelLoader = new DefaultModelLoader();

		long time = System.currentTimeMillis();

		Map<String, EventSerializer> serializers = new HashMap<>();
		serializers.put(NodeAddedEvent.EVENT_TYPE, new NodeAddedEventSerializer());
		serializers.put(NodeUpdatedEvent.EVENT_TYPE, new NodeUpdatedEventSerializer());
		serializers.put(NodeRemovedEvent.EVENT_TYPE, new NodeRemovedEventSerializer());
		serializers.put(NodeContentPutEvent.EVENT_TYPE, new NodeContentPutEventSerializer());
		EventsSerializer eventsSerializer = new EventsSerializer();
		eventsSerializer.setSerializers(serializers);

		this.eventsDAO = new MongoEventsDAO(db, "events" + time, eventsSerializer);
		MongoEntitiesDAO entitiesDAO = new MongoEntitiesDAO(db, "entities" + time);
		MongoSimilarityDAO similarityDAO = new MongoSimilarityDAO(db, "similarity" + time);

		EntitiesServiceImpl entitiesService = new EntitiesServiceImpl(ExtracterType.StanfordNLP.toString(),
				modelLoader, entitiesDAO, similarityDAO, contentGetter);
		this.entitiesService = entitiesService;

		UserTrackingService userTrackingService = new UserTrackingService()
		{
			@Override
            public void handleContentGet(NodeContentGetEvent event)
            {
            }
		};

//		this.eventsListener = new EventsListener(eventsDAO, entitiesService, userTrackingService);
	}

	@Test
	public void test1() throws Exception
	{
		contentGetter
			.addTestContent(1, "1", "1", "path", "I like San Francisco but I like Hawaii more", "text/plain")
			.addTestContent(2, "2", "2", "path1", "I like San Francisco but I like Hawaii more", "text/plain");

		String txnId = GUID.generate();

		{
			NodeContentPutEvent nodeEvent = new NodeContentPutEvent();
			nodeEvent.setTxnId(txnId);
			nodeEvent.setNodeInternalId(1);
			nodeEvent.setNodeId("1");
			nodeEvent.setVersionLabel("1");
			nodeEvent.setNodeType("cm:content");
			eventsDAO.addEvent(nodeEvent);
//			eventsListener.onEvent(nodeEvent);
		}

		{
			NodeContentPutEvent nodeEvent = new NodeContentPutEvent();
			nodeEvent.setTxnId(txnId);
			nodeEvent.setNodeInternalId(2);
			nodeEvent.setNodeId("2");
			nodeEvent.setVersionLabel("2");
			nodeEvent.setNodeType("cm:content");
	         eventsDAO.addEvent(nodeEvent);
//			eventsListener.onEvent(nodeEvent);
		}

		TransactionCommittedEvent txnCommittedEvent = new TransactionCommittedEvent();
		txnCommittedEvent.setTxnId(txnId);
        eventsDAO.addEvent(txnCommittedEvent);
//		eventsListener.onEvent(txnCommittedEvent);

		Node node1 = Node.build().nodeId("1").versionLabel("1");
		Node node2 = Node.build().nodeId("2").versionLabel("2");
		double similarity = entitiesService.getSimilarity(node1, node2);
		assertEquals(1.0, similarity, 0.00001);
	}

//	@Test
	public void test2() throws Exception
	{
		contentGetter
			.addTestContent(1, "1", "1", "/Users/sglover/Documents/PublicApiBenchmark.txt",
			        new File("/Users/sglover/Documents/PublicApiBenchmark.txt"), "text/plain")
			.addTestContent(2, "2", "2", "/Users/sglover/Documents/UsingVoltDB.txt",
			        new File("/Users/sglover/Documents/UsingVoltDB.txt"), "text/plain");

		String txnId = GUID.generate();

		{
			NodeAddedEvent nodeEvent = new NodeAddedEvent();
			nodeEvent.setTxnId(txnId);
			nodeEvent.setNodeInternalId(1);
			nodeEvent.setNodeId("1");
			nodeEvent.setVersionLabel("1");
			nodeEvent.setNodeType("cm:content");
			eventsDAO.addEvent(nodeEvent);
//			eventsListener.onEvent(nodeEvent);
		}

		{
			NodeAddedEvent nodeEvent = new NodeAddedEvent();
			nodeEvent.setTxnId(txnId);
			nodeEvent.setNodeInternalId(2);
			nodeEvent.setNodeId("2");
			nodeEvent.setVersionLabel("2");
			nodeEvent.setNodeType("cm:content");
            eventsDAO.addEvent(nodeEvent);
//			eventsListener.onEvent(nodeEvent);
		}

		TransactionCommittedEvent txnCommittedEvent = new TransactionCommittedEvent();
		txnCommittedEvent.setTxnId(txnId);
        eventsDAO.addEvent(txnCommittedEvent);
//		eventsListener.onEvent(txnCommittedEvent);

		Node node1 = Node.build().nodeId("1").versionLabel("1");
		Node node2 = Node.build().nodeId("2").versionLabel("2");
		double similarity = entitiesService.getSimilarity(node1, node2);
		System.out.println(similarity);
//		assertEquals(1.0, similarity, 0.00001);
	}

	@Test
	public void test3() throws Exception
	{
		contentGetter
			.addTestContent(1, "1", "1", "/Users/sglover/Documents/UsingVoltDB.txt",
			        new File("/Users/sglover/Documents/UsingVoltDB.txt"), "text/plain")
			.addTestContent(2, "2", "2", "/Users/sglover/Documents/UsingVoltDB.txt",
			        new File("/Users/sglover/Documents/UsingVoltDB.txt"), "text/plain");

		String txnId = GUID.generate();

		{
			NodeContentPutEvent nodeEvent = new NodeContentPutEvent();
			nodeEvent.setTxnId(txnId);
			nodeEvent.setNodeInternalId(1);
			nodeEvent.setNodeId("1");
			nodeEvent.setVersionLabel("1");
			nodeEvent.setNodeType("cm:content");
            eventsDAO.addEvent(nodeEvent);
//			eventsListener.onEvent(nodeEvent);
		}

		{
			NodeContentPutEvent nodeEvent = new NodeContentPutEvent();
			nodeEvent.setTxnId(txnId);
			nodeEvent.setNodeInternalId(2);
			nodeEvent.setNodeId("2");
			nodeEvent.setVersionLabel("2");
			nodeEvent.setNodeType("cm:content");
            eventsDAO.addEvent(nodeEvent);
//			eventsListener.onEvent(nodeEvent);
		}

		TransactionCommittedEvent txnCommittedEvent = new TransactionCommittedEvent();
		txnCommittedEvent.setTxnId(txnId);
        eventsDAO.addEvent(txnCommittedEvent);
//		eventsListener.onEvent(txnCommittedEvent);

		Node node1 = Node.build().nodeId("1").versionLabel("1");
		Node node2 = Node.build().nodeId("2").versionLabel("2");
		double similarity = entitiesService.getSimilarity(node1, node2);
		System.out.println(similarity);
//		assertEquals(1.0, similarity, 0.00001);
	}

	@Test
	public void test4() throws Exception
	{
		contentGetter
			.addTestContent(1, "1", "1", "/Users/sglover/Documents/Performance Appraisal Review-1-2011.txt",
			        new File("/Users/sglover/Documents/Performance Appraisal Review-1-2011.txt"), "text/plain")
			.addTestContent(2, "2", "2", "/Users/sglover/Documents/nosql_brownbag.txt",
			        new File("/Users/sglover/Documents/nosql_brownbag.txt"), "text/plain");

		String txnId = GUID.generate();

		{
			NodeContentPutEvent nodeEvent = new NodeContentPutEvent();
			nodeEvent.setTxnId(txnId);
			nodeEvent.setNodeInternalId(1);
			nodeEvent.setNodeId("1");
			nodeEvent.setVersionLabel("1");
			nodeEvent.setNodeType("cm:content");
			eventsDAO.addEvent(nodeEvent);
//			eventsListener.onEvent(nodeEvent);
		}

		{
			NodeContentPutEvent nodeEvent = new NodeContentPutEvent();
			nodeEvent.setTxnId(txnId);
			nodeEvent.setNodeInternalId(2);
			nodeEvent.setNodeId("2");
			nodeEvent.setVersionLabel("2");
			nodeEvent.setNodeType("cm:content");
			eventsDAO.addEvent(nodeEvent);
//			eventsListener.onEvent(nodeEvent);
		}

		TransactionCommittedEvent txnCommittedEvent = new TransactionCommittedEvent();
		txnCommittedEvent.setTxnId(txnId);
        eventsDAO.addEvent(txnCommittedEvent);
//		eventsListener.onEvent(txnCommittedEvent);

		Node node1 = Node.build().nodeId("1").versionLabel("1");
		Node node2 = Node.build().nodeId("2").versionLabel("2");
		double similarity = entitiesService.getSimilarity(node1, node2);
		System.out.println(similarity);
//		assertEquals(1.0, similarity, 0.00001);
	}
}