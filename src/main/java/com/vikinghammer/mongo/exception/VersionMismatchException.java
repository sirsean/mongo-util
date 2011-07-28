package com.vikinghammer.mongo.exception;

/**
 * When you update a MongoModel object, we only save it to the database if
 * both the id and the version number are correct. If you attempt to save
 * a document with an outdated version number, it means someone else has saved
 * that document since you loaded it, and we won't let you commit it. So if
 * you get one of these, you'll have to handle it by loading the record and
 * trying again.
 * 
 * @author Sean Schulte
 */
public class VersionMismatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new VersionMismatchException with no detail message.
     */
    public VersionMismatchException() {
        super();
    }

    /**
     * Creates a new VersionMismatchException with a detail message built
     * based on the given id and version.
     *
     * @param id - the id of the MongoModel that was being saved
     * @param version - the version number of the MongoModel that was being saved (this is the stale version number, not the latest version number stored in the DB)
     */
    public VersionMismatchException(Object id, Object version) {
        super(String.format("Attempt to update record %s failed, version %s is stale.", id, version));
    }
	
}
