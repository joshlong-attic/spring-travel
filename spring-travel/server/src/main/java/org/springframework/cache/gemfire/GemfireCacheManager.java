package org.springframework.cache.gemfire;

import com.gemstone.gemfire.cache.Region;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides an implementation of the Spring 3.1 {@link org.springframework.cache.CacheManager} interface.
 * <p/>
 * This interface can be used in conjunction with Spring 3.1's declarative cache management facilities to cache method return values, independant of the underlying persistence technology.
 *
 * @author Josh Long
 * @see org.springframework.cache.CacheManager
 */
public class GemfireCacheManager extends AbstractCacheManager {

	// Regions
	private Set<Region<?,?>> regions =new LinkedHashSet<Region<?, ?>>();

	// setup the Gemfire Cache (this should be the parent of the Regions in the collection!
	private com.gemstone.gemfire.cache.Cache gfeCache;

	@Override
	public void afterPropertiesSet() {
		Assert.notNull(this.regions,  "'regions' should not be null");

		if(this.regions.size()  == 0)
			regions.addAll( this.gfeCache.rootRegions()) ;

		super.afterPropertiesSet();
	}

	public void setGemfireCache(com.gemstone.gemfire.cache.Cache gfeCache) {
		this.gfeCache = gfeCache;
		Assert.notNull(this.gfeCache, "the 'gemfireCache' property must not be null.");
	}

	/**
	 * optional: if you want to bootstrap the collection by simply examining the {@link com.gemstone.gemfire.cache.Cache#rootRegions()},
	 * then ignore this property. If you only want specific {@link Region}s used, then
	 * you can set this property.
	 * @param r     the collection of Regions to expose to the Spring Cache abstraction. By default, this will be set by introspecting the Cache
	 */
	public void setRegions(Set<Region<?, ?>> r) {
		Assert.notNull( r , "the 'regions' collection must not be null");
		for (Region<?,?> region  :  r)
			this.regions.add(region);
	}


	@Override
	protected Collection<Cache> loadCaches() {
		Collection<Cache> caches = new LinkedHashSet<Cache>();
		for (Region<?, ?> r : regions)
			caches.add(new GemfireRegionCache(r));
		return caches;
	}

}
