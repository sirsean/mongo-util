package com.vikinghammer.mongo.query;

import java.util.List;

import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import com.vikinghammer.mongo.model.MongoModel;

/**
 * 
 * 
 * @author sschulte
 */
public interface MongoQueryRunner<T extends MongoModel> {

    public T one(DBObject query);

    public T one(DBObject query, DBObject sort);

    public List<T> list(Iterable<DBObject> cursor);

    public List<T> list(DBObject query);

    public List<T> list(DBObject query, DBObject sort);

}
