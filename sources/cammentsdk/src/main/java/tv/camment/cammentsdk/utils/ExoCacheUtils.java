package tv.camment.cammentsdk.utils;

import tv.camment.cammentsdk.exoplayer.upstream.cache.Cache;
import tv.camment.cammentsdk.exoplayer.upstream.cache.LeastRecentlyUsedCacheEvictor;
import tv.camment.cammentsdk.exoplayer.upstream.cache.SimpleCache;

public class ExoCacheUtils {

    private static final ExoCacheUtils instance = new ExoCacheUtils();

    private final Cache cache;

    public static ExoCacheUtils getInstance() {
        return instance;
    }

    private ExoCacheUtils() {
        cache = new SimpleCache(FileUtils.getInstance().getCacheDirFile(), new LeastRecentlyUsedCacheEvictor(50 * 1000 * 1024)); //50 MB
    }

    public Cache getCache() {
        return cache;
    }
    
}
