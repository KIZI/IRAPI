package eu.linkedtv.irapi.search.querying;

import java.util.NoSuchElementException;

/**
 * This interface defines a index pool service.
 *
 * @author babu
 *
 */
public interface IndexPool {

	/**
	 *
	 * @param indexIdentifier
	 * @return proxy for index of any type
	 * @throws NoSuchElementException
	 *             -when the index is not found in pool
	 */
	IndexProxy getIndexProxy(String indexIdentifier) throws NoSuchElementException;

}
