package com.vikinghammer.mongo.model;

import com.mongodb.DBCursor;

/**
 * This is a non-serializable page for internal use by MongoCollection.
 *
 * Since DBCursor is not serializable, we can't send it over the wire;
 * plus, we wouldn't want to anyway. So this DBPage object is returned by
 * MongoCollection, and at that point it'll be fully populated by its
 * metadata fields (describing how many items there are in total, as well
 * as the page number/size that was requested), and a cursor that gives
 * access to the records found by the query.
 *
 * You can then get a serializable Page object, which you'll need to populate
 * with converted model objects based on the cursor from DBPage.
 * 
 * @author Sean Schulte
 */
public class DBPage {

    private DBCursor _cursor;
    private Long _totalItems;
    private Integer _pageNumber;
    private Integer _pageSize;

    public DBPage() {
        super();
    }

    /**
     * Create an instance of the serializable Page object, based
     * on the fields in this DBPage. The only thing you'll need
     * to do with it is populate its items (from the cursor).
     *
     * @return a serializable Page
     */
    public Page getPage() {
        Page page = new Page();
        page.setTotalItems(_totalItems);
        page.setPageNumber(_pageNumber);
        page.setPageSize(_pageSize);
        return page;
    }

    public DBCursor getCursor() {
        return _cursor;
    }

    public void setCursor(DBCursor cursor) {
        _cursor = cursor;
    }

    public Long getTotalItems() {
        return _totalItems;
    }

    public void setTotalItems(Long totalItems) {
        _totalItems = totalItems;
    }

    public Integer getPageNumber() {
        return _pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        _pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return _pageSize;
    }

    public void setPageSize(Integer pageSize) {
        _pageSize = pageSize;
    }
	
}
