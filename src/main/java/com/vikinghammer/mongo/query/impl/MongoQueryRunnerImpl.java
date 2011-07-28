package com.vikinghammer.mongo.query.impl;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vikinghammer.mongo.collection.MongoCollection;
import com.vikinghammer.mongo.factory.ModelFactory;
import com.vikinghammer.mongo.model.MongoModel;
import com.vikinghammer.mongo.query.MongoQueryRunner;

import com.mongodb.DBObject;
import com.mongodb.DBCursor;

/**
 * 
 * 
 * @author sschulte
 */
public class MongoQueryRunnerImpl<T extends MongoModel> implements MongoQueryRunner<T> {

	private Logger _log = LoggerFactory.getLogger(getClass());

    private MongoCollection _collection;
    private ModelFactory<T> _modelFactory;

    public MongoQueryRunnerImpl(
        MongoCollection collection,
        ModelFactory<T> modelFactory
    ) {
        super();
        _collection = collection;
        _modelFactory = modelFactory;
    }

    @Override
    public T one(DBObject query) {
        DBObject doc = _collection.findOne(query);

        return _modelFactory.build(doc);
    }

    @Override
    public T one(DBObject query, DBObject sort) {
        List<DBObject> list = _collection.find(query).sort(sort).limit(1).toArray();
        if (!list.isEmpty()) {
            return _modelFactory.build(list.get(0));
        } else {
            return null;
        }
    }

    @Override
    public List<T> list(Iterable<DBObject> cursor) {
        List<T> models = new ArrayList<T>();
        for (DBObject obj : cursor) {
            models.add(_modelFactory.build(obj));
        }
        return models;
    }

    @Override
    public List<T> list(DBObject query) {
        DBCursor cursor = _collection.find(query);
        return list(cursor);
    }

    @Override
    public List<T> list(DBObject query, DBObject sort) {
        DBCursor cursor = _collection.find(query).sort(sort);
        return list(cursor);
    }

}
