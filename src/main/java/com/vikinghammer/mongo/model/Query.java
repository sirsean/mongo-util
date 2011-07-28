package com.vikinghammer.mongo.model;

import java.io.Serializable;

import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

/**
 * An object to help build a query to get objects stored in a Mongo DB.
 * 
 * @author Sean Schulte
 */
public class Query implements Serializable {

    private static final long serialVersionUID = 1L;

    private DBObject _queryMap;
    private Integer _pageSize;
    private Integer _pageNumber;

    public Query() {
        super();
        _queryMap = new BasicDBObject();
        _pageSize = 100;
        _pageNumber = 1;
    }

    /**
     * Convenience method for searching by equality. This is done differently
     * because the same key can't be equal to two values, and it makes no sense
     * to query a single key based on both equality and something else.
     *
     * @param key - the key to search against
     * @param value - search for anything matching this value
     * @return this Query object, for chaining
     */
    public Query eq(String key, Object value) {
        // we treat this case differently because $eq isn't supported
        // and if you're querying for equality, there's no reason to allow other comparison operators
        _queryMap.put(key, value);

        return this;
    }

    public void setPageSize(Integer pageSize) {
        _pageSize = pageSize;
    }

    public Integer getPageSize() {
        return _pageSize;
    }

    public void setPageNumber(Integer pageNumber) {
        _pageNumber = pageNumber;
    }

    public Integer getPageNumber() {
        return _pageNumber;
    }

    /**
     * The "skip" value is how many results in the query to skip before
     * returning results; it's calculated based on the page size and 
     * page number.
     *
     * @return the skip value for this query
     */
    public Integer getSkip() {
        return ((_pageNumber - 1) * _pageSize);
    }

    /**
     * The "limit" value is simply the size of a page.
     *
     * @return the limit value for this query
     */
    public Integer getLimit() {
        return _pageSize;
    }

    /**
     * A convenience method to easily add a less-than comparison to this query.
     *
     * @param key - the key you're searching against
     * @param value - matching everything less than this value
     * @return this Query object, for chaining
     */
    public Query lt(String key, Object value) {
        return add("$lt", key, value);
    }
    
    /**
     * A convenience method to easily add a greater-than comparison to this query.
     *
     * @param key - the key you're searching against
     * @param value - matching everything greater than this value
     * @return this Query object, for chaining
     */
    public Query gt(String key, Object value) {
        return add("$gt", key, value);
    }

    /**
     * This is the query object that you can actually send to Mongo to
     * perform the query.
     *
     * @return a DBObject representing this query that you can send to Mongo
     */
    public DBObject getQuery() {
        return _queryMap;
    }

    /**
     * Add a comparison into the query. You can have multiple comparators for
     * the same key, but you can't have multiple of the SAME comparator for a
     * single key.
     *
     * @param comparison - $lt, $gt
     * @param key
     * @param value
     */
    public Query add(String comparison, String key, Object value) {
        DBObject obj = new BasicDBObject();
        if (_queryMap.containsKey(key)) {
            obj = (DBObject)_queryMap.get(key);
        }
        obj.put(comparison, value);
        _queryMap.put(key, obj);

        return this;
    }
	
}
