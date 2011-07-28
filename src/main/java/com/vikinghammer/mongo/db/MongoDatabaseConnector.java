package com.vikinghammer.mongo.db;

import java.util.List;
import java.util.ArrayList;

import java.net.UnknownHostException;

import org.springframework.util.Assert;

import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.ServerAddress;

/**
 * Effectively a wrapper for Mongo's DB, which allows you to configure
 * your database using Spring, and inject them into your collections.
 * 
 * Note that the init() method MUST be called, to instantiate the DB object
 * we use to communicate with the Mongo database.
 *
 * @author Sean Schulte
 */
public class MongoDatabaseConnector {

    private String _host;
    private String _databaseName;

    private boolean _autoConnectRetry;
    private int _connectionsPerHost;
    private int _connectTimeout;
    private int _maxWaitTime;
    private int _socketTimeout;
    private int _threadsAllowedToBlockForConnectionMultiplier;
    private int _maxConnectionRetryTime;

    private Mongo _connection;
    private DB _db;

    public MongoDatabaseConnector() {
        super();
    }

    /**
     * Check that we have the information we need (the hostname and database name),
     * then connect to the Mongo server and the specified database.
     */
    public void init() throws UnknownHostException {
        Assert.notNull(_host, "Must specify a host");
        Assert.notNull(_databaseName, "Must specify a database name");

        MongoOptions options = new MongoOptions();
        options.autoConnectRetry = _autoConnectRetry;
        options.connectionsPerHost = _connectionsPerHost;
        options.connectTimeout = _connectTimeout;
        options.maxWaitTime = _maxWaitTime;
        options.socketTimeout = _socketTimeout;
        options.threadsAllowedToBlockForConnectionMultiplier = _threadsAllowedToBlockForConnectionMultiplier;
        // TODO: activate this once the maxConnectionRetryTime is accepted into mongo-java-driver
        //options.maxConnectionRetryTime = _maxConnectionRetryTime;

        // split the hostname on comma, because they can specify a replica set
        String[] hostnames = _host.split(",");
        List<ServerAddress> hosts = new ArrayList<ServerAddress>();
        for (String hostname : hostnames) {
            hosts.add(new ServerAddress(hostname));
        }

        // if we use the list of ServerAddress, Mongo thinks it's connecting to a Replica Set,
        // but if it's not a Replica Set, that's a problem. So we're only connecting as a Replica
        // Set if they supply multiple hostnames
        if (hosts.size() > 1) {
            _connection = new Mongo(hosts, options);
            _connection.slaveOk();
        } else {
            _connection = new Mongo(_host, options);
        }

        _db = _connection.getDB(_databaseName);
    }

    /**
     * Get a connection to a collection, by its name.
     *
     * @param collectionName - the name of the collection you want
     * @return a DBCollection you can query against
     */
    public DBCollection getCollection(String collectionName) {
        return _db.getCollection(collectionName);
    }

    public void setHost(String host) {
        _host = host;
    }

    public void setDatabaseName(String databaseName) {
        _databaseName = databaseName;
    }

    public void setAutoConnectRetry(boolean autoConnectRetry) {
        _autoConnectRetry = autoConnectRetry;
    }

    public void setConnectionsPerHost(int connectionsPerHost) {
        _connectionsPerHost = connectionsPerHost;
    }

    public void setConnectTimeout(int connectTimeout) {
        _connectTimeout = connectTimeout;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        _maxWaitTime = maxWaitTime;
    }

    public void setSocketTimeout(int socketTimeout) {
        _socketTimeout = socketTimeout;
    }

    public void setThreadsAllowedToBlockForConnectionMultiplier(int threadsAllowedToBlockForConnectionMultiplier) {
        _threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
    }

    public void setMaxConnectionRetryTime(int maxConnectionRetryTime) {
        _maxConnectionRetryTime = maxConnectionRetryTime;
    }
	
}
