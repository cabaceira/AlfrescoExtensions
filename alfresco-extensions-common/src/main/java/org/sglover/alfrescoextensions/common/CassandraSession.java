/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.alfrescoextensions.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

/**
 * 
 * @author sglover
 *
 */
@Component
public class CassandraSession
{
    @Value("${cassandra.host}")
    private String host;

    @Value("${cassandra.keyspace}")
    private String keyspace;

    private boolean recreate = false;

    private Cluster cluster;
    private Session cassandraSession;

    public CassandraSession(String host, String keyspace, boolean recreate)
    {
        this.host = host;
        this.keyspace = keyspace;
        this.recreate = recreate;
    }

    public CassandraSession()
    {
    }

    public CassandraSession(boolean recreate)
    {
        this.recreate = recreate;
    }

    @PostConstruct
    public void init()
    {
        this.cassandraSession = buildCassandraSession(host);

        if(recreate)
        {
            deleteKeyspace();
            createKeyspace();
        }
        else
        {
            if(cassandraSession.getCluster().getMetadata().getKeyspace(keyspace) == null)
            {
                createKeyspace();
            }
        }
    }

    public String getKeyspace()
    {
        return keyspace;
    }

    private Session buildCassandraSession(String host)
    {
        this.cluster = Cluster
                .builder()
                .addContactPoint(host)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(
                         new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
                .build();
        Session session = cluster.connect();
        return session;
    }

    private void deleteKeyspace()
    {
        cassandraSession.execute("DROP KEYSPACE IF EXISTS " + keyspace + ";");
//        KeyspaceMetadata keySpaceMetadata = cassandraSession.getCluster().getMetadata().getKeyspace("alfresco");
//        if(keySpaceMetadata != null)
//        {
//            cassandraSession.execute("DROP KEYSPACE IF EXISTS alfresco;");
//        }
        KeyspaceMetadata keySpaceMetadata = null;
        do
        {
            keySpaceMetadata = cassandraSession.getCluster().getMetadata().getKeyspace(keyspace);
        }
        while(keySpaceMetadata != null);
    }

    private void createKeyspace()
    {
        cassandraSession.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace + " WITH replication "
                + "= {'class':'SimpleStrategy', 'replication_factor':3};");
    }

    public Cluster getCluster()
    {
        return cluster;
    }

    public Session getCassandraSession()
    {
        return cassandraSession;
    }

    public void shutdown()
    {
        cassandraSession.getCluster().close();
    }
}
