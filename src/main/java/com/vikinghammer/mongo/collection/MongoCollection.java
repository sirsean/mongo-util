package com.vikinghammer.mongo.collection;

import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.Assert;

import com.vikinghammer.mongo.db.MongoDatabaseConnector;
import com.vikinghammer.mongo.model.Query;
import com.vikinghammer.mongo.model.DBPage;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.vikinghammer.mongo.exception.VersionMismatchException;

/**
 * Effectively a wrapper for Mongo's DBCollection, which allows you to
 * configure your collections using Spring, and inject them into your
 * services.
 *
 * Note that the init() method MUST be called, to instantiate the DBCollection
 * we use to communicate with the Mongo database.
 * 
 * @author Sean Schulte
 */
public class MongoCollection {

	private Logger _log = LoggerFactory.getLogger(getClass());

    private MongoDatabaseConnector _databaseConnector;
    private String _collectionName;
    private List<String> _indices;

    private DBCollection _collection;

    public MongoCollection() {
        super();
    }

    /**
     * Check that we have the information we need (the database connector and the
     * collection name), and instantiate the DBCollection to communicate with the
     * database.
     */
    public void init() {
        Assert.notNull(_databaseConnector, "Must specify a database connector");
        Assert.notNull(_collectionName, "Must specify a collection name");

        _collection = _databaseConnector.getCollection(_collectionName);

        _ensureIndices();
    }

    /**
     * Save a single object to the database. This will work for either new objects
     * or existing ones (which will be updated).
     *
     * @param object - the record to be saved
     */
    public void save(DBObject object) {
        if (object.get("version") == null) {
            object.put("version", 0);
            _collection.save(object);
        } else {
            // get the current version
            Integer currentVersion = (Integer)object.get("version");

            // increment the version number on the object we're saving
            object.put("version", currentVersion + 1);

            // build a query object to match against the id and current version
            DBObject query = new BasicDBObject();
            query.put("_id", object.get("_id"));
            query.put("version", currentVersion);

            // this will only let the update go through if the version matches
            // we need to call requestStart/requestDone to make sure these two commands run on the same thread, so we know we're checking the right collection in the pool for the error message
            _collection.getDB().requestStart();
            _collection.update(query, object);
            DBObject error = _collection.getDB().getLastError();
            _collection.getDB().requestDone();

            // if we detect that the update didn't happen because of a version mismatch, we throw a VersionMismatchException
            if (!((Boolean)error.get("updatedExisting"))) {
                throw new VersionMismatchException(object.get("_id"), currentVersion);
            }
        }
    }

    /**
     * Get a cursor representing a set of records matching the given query.
     *
     * @param query - a standard Mongo query
     * @return a cursor object giving access to all the records matching the given query
     */
    public DBCursor find(DBObject query) {
        return _collection.find(query);
    }

    /**
     * Get a cursor representing a set of records matching the given query, and
     * support paging with skip and limit values.
     *
     * @param query - a standard Mongo query
     * @param limit - the limit value to pass in with the query
     * @param skip - the skip value to pass in with the query
     * @return all the records matching the given query
     */
    public DBCursor find(DBObject query, Integer limit, Integer skip) {
        return _collection.find(query).skip(skip).limit(limit);
    }

    /**
     * Make a paginated query. The resulting DBPage is not serializable; before
     * you use/return it, you'll want to convert it into a serializable Page object.
     *
     * @param query - a mongo-util query that contains a MongoDB query object and also specifies the page size/number
     * @return DBPage describing the total number of items as well as a cursor giving access to the requested page
     */
    public DBPage find(Query query) {
        DBPage page = new DBPage();
        page.setCursor(_collection.find(query.getQuery()).skip(query.getSkip()).limit(query.getLimit()));
        page.setTotalItems(getCount(query.getQuery()));
        page.setPageNumber(query.getPageNumber());
        page.setPageSize(query.getPageSize());
        return page;
    }

    /**
     * Get a single record.
     *
     * @param query - a standard Mongo query
     * @return the first record matching the query, or null if none are found
     */
    public DBObject findOne(DBObject query) {
        return _collection.findOne(query);
    }

    /**
     * Get the number of records that would be returned by the given query.
     *
     * @param query - a standard Mongo query
     * @return the number of records matching the query
     */
    public long getCount(DBObject query) {
        return _collection.getCount(query);
    }

    /**
     * This is a destructive method that deletes all the records in the collection.
     * I use it in the before/after triggers in my tests that use an actual Mongo
     * database. You probably shouldn't ever call it in your programs.
     */
    public void removeAllRecords() {
        _collection.remove(new BasicDBObject());
    }

    /**
     * Remove all the records that match the given query.
     *
     * @param query - a standard Mongo query
     */
    public void remove(DBObject query) {
        _collection.remove(query);
    }

    /**
     * Execute a map/reduce query against the Mongo collection. It will save the results
     * into the collection specified by collectionName.
     *
     * @param mapFunction
     * @param reduceFunction
     * @param collectionName replace the values in this collection
     * @param query limits the inputs to the map function
     */
    public void mapReduce(
        String mapFunction,
        String reduceFunction,
        String collectionName,
        DBObject query
    ) {
        _collection.mapReduce(
            mapFunction,
            reduceFunction,
            collectionName,
            query
        );
    }

    /**
     * Execute a map/reduce query and return the results inline.
     *
     * @param mapFunction
     * @param reduceFunction
     * @param query limits the inputs to the map function
     * @return a DBCursor containing the results of the map/reduce
     */
    public Iterable<DBObject> mapReduce(
        String mapFunction,
        String reduceFunction,
        DBObject query
    ) {
        MapReduceCommand command = new MapReduceCommand(
            _collection,
            mapFunction,
            reduceFunction,
            null,
            MapReduceCommand.OutputType.INLINE,
            query
        );
        
        MapReduceOutput output = _collection.mapReduce(command);
        _log.debug(String.format("Inline map/reduce: %s", output.toString()));
        return output.results();
    }

    /**
     * If any indices have been supplied, we want to ensure that the collection has
     * an index on each of them.
     *
     * This supports multi-field keys, by comma-delimiting the index. For example, to
     * put an index on just "fieldOne" you would pass "fieldOne" as the index, and to
     * put an index on each of "fieldOne" and "fieldTwo" you would pass each of those
     * in the array of indices. But if you wanted one index on both "fieldOne" and "fieldTwo"
     * then you should pass "fieldOne,fieldTwo" as the index.
     */
    private void _ensureIndices() {
        if (_indices != null) {
            for (String index : _indices) {
                _log.info(String.format("Ensuring index on collection %s: %s", _collection.getFullName(), index));
                String[] arrayOfIndices = index.split(",");
                if (arrayOfIndices.length == 1) {
                    _collection.ensureIndex(index);
                } else {
                    DBObject keys = new BasicDBObject();
                    for (int i=0; i < arrayOfIndices.length; i++) {
                        keys.put(arrayOfIndices[i], 1);
                    }
                    _collection.ensureIndex(keys);
                }
            }
        }
    }

    public void setDatabaseConnector(MongoDatabaseConnector connector) {
        _databaseConnector = connector;
    }

    public void setCollectionName(String collectionName) {
        _collectionName = collectionName;
    }

    public void setIndices(List<String> indices) {
        _indices = indices;
    }
	
}
