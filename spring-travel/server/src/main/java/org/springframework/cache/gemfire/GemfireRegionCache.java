package org.springframework.cache.gemfire;

import com.gemstone.gemfire.cache.Region;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.DefaultValueWrapper;
import org.springframework.util.Assert;

/**
 * Simple implementation of a {@link org.springframework.cache.Cache} in the Spring Cache API. This delegates to a shared region, which can in turn be subdivided.
 * <p/>
 * It depends, generally on what the preferred approach here is. Perhaps using GemFire {@link com.gemstone.gemfire.cache.Cache} caches is a more natural mapping?
 *
 * @author Josh Long
 * @see org.springframework.cache.Cache
 */           @Deprecated
public class GemfireRegionCache implements Cache {

	private volatile Region<?, ?> region;

	/**
	 * holds a value (any value, including nulls)
	 * {@link ValueWrapper}
	 */

	@Override
	public String getName() {
		return this.region.getName();
	}

	@Override
	public Object getNativeCache() {
		return this.region;
	}

	@Override
	public ValueWrapper get(Object key) {

		Object result = this.region.get(key) ;
		if(null ==result )
			return null ;
		return new DefaultValueWrapper(result) ;

		///return new GemfireValueWrapper(this.region.get(key));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void put(Object key, Object value) {
		((Region<Object, Object>) this.region).put(key, value);
	}

	@Override
	public void evict(Object key) {
		this.region.remove(key);
	}

	@Override
	public void clear() {
		this.region.clear();
	}

	/**
	 * Requires a Gemfire {@link Region}
	 *
	 * @param region the region to interact with.
	 */
	public GemfireRegionCache(Region<?, ?> region) {
		this.region = region;
		Assert.notNull(this.region, "the region can't be null");
	}


}
