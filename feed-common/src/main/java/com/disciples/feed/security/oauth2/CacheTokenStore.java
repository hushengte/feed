package com.disciples.feed.security.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.Assert;

/**
 * Copy of InMemoryTokenStore, with map calls replaced with calls to cache. 
 * Using cache API directly instead of annotations as need to check for nulls etc Can use any backing cache as long as its initialized. 
 */
public class CacheTokenStore implements TokenStore, InitializingBean {

	private static final int DEFAULT_FLUSH_INTERVAL = 99000;

	private static final Set<OAuth2AccessToken> OA_TOKEN_EMPTY_LST = Collections.<OAuth2AccessToken> emptySet();

	private Cache accessTokenCache;

	private Cache authenticationToAccessTokenCache;

	private Cache userNameToAccessTokenCache;

	private Cache clientIdToAccessTokenCache;

	private Cache refreshTokenCache;

	private Cache accessTokenToRefreshTokenCache;

	private Cache refreshTokenAuthenticationCache;

	private Cache refreshTokenToAccessTokenCache;

	private Cache authenticationCache;

	private final DelayQueue<TokenExpiry> expiryQueue = new DelayQueue<TokenExpiry>();

	private final ConcurrentHashMap<String, TokenExpiry> expiryMap = new ConcurrentHashMap<String, TokenExpiry>();

	private CacheManager cacheManager;

	private int flushInterval = DEFAULT_FLUSH_INTERVAL;

	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

	private AtomicInteger flushCounter = new AtomicInteger(0);

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(cacheManager != null, "Property 'cacheManager' cannot be null.");
		
		accessTokenCache = cacheManager.getCache("accessTokenCache");
		authenticationToAccessTokenCache = cacheManager.getCache("authenticationToAccessTokenCache");
		userNameToAccessTokenCache = cacheManager.getCache("userNameToAccessTokenCache");
		clientIdToAccessTokenCache = cacheManager.getCache("clientIdToAccessTokenCache");
		refreshTokenCache = cacheManager.getCache("refreshTokenCache");
		accessTokenToRefreshTokenCache = cacheManager.getCache("accessTokenToRefreshTokenCache");
		refreshTokenAuthenticationCache = cacheManager.getCache("refreshTokenAuthenticationCache");
		authenticationCache = cacheManager.getCache("authenticationCache");
	}

	/**
	 * The number of tokens to store before flushing expired tokens. Defaults to 1000.
	 * 
	 * @param flushInterval
	 *            the interval to set
	 */
	public void setFlushInterval(int flushInterval) {
		this.flushInterval = flushInterval;
	}

	/**
	 * The interval (count of token inserts) between flushing expired tokens.
	 * 
	 * @return the flushInterval the flush interval
	 */
	public int getFlushInterval() {
		return flushInterval;
	}

	/**
	 * Convenience method for super admin users to remove all tokens (useful for testing, not really in production)
	 */
	public void clear() {
		accessTokenCache.clear();
		authenticationToAccessTokenCache.clear();
		clientIdToAccessTokenCache.clear();
		refreshTokenCache.clear();
		accessTokenToRefreshTokenCache.clear();
		authenticationCache.clear();
		refreshTokenAuthenticationCache.clear();
		refreshTokenToAccessTokenCache.clear();
		expiryQueue.clear();
	}

	public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
		this.authenticationKeyGenerator = authenticationKeyGenerator;
	}

	public int getExpiryTokenCount() {
		return expiryQueue.size();
	}

	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		String key = authenticationKeyGenerator.extractKey(authentication);
		ValueWrapper vw = authenticationToAccessTokenCache.get(key);
		if (vw == null){
			return null;
		}
		OAuth2AccessToken accessToken = (OAuth2AccessToken) vw.get();
		if (accessToken != null && !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))) {
			// Keep the stores consistent (maybe the same user is represented by this authentication but the details have changed)
			storeAccessToken(accessToken, authentication);
		}
		return accessToken;
	}

	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		return readAuthentication(token.getValue());
	}

	public OAuth2Authentication readAuthentication(String token) {
		ValueWrapper vw = this.authenticationCache.get(token);
		if (vw == null){
			return null;
		}
		return (OAuth2Authentication) vw.get();
	}

	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
		return readAuthenticationForRefreshToken(token.getValue());
	}

	public OAuth2Authentication readAuthenticationForRefreshToken(String token) {
		ValueWrapper vw = this.refreshTokenAuthenticationCache.get(token);
		if (vw == null){
			return null;
		}
		return (OAuth2Authentication) vw.get();
	}

	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		if (this.flushCounter.incrementAndGet() >= this.flushInterval) {
			flush();
			this.flushCounter.set(0);
		}
		this.accessTokenCache.put(token.getValue(), token);
		this.authenticationCache.put(token.getValue(), authentication);
		this.authenticationToAccessTokenCache.put(authenticationKeyGenerator.extractKey(authentication), token);
		if (!authentication.isClientOnly()) {
			addToCollection(this.userNameToAccessTokenCache, getApprovalKey(authentication), token);
		}
		addToCollection(this.clientIdToAccessTokenCache, authentication.getOAuth2Request().getClientId(), token);
		if (token.getExpiration() != null) {
			TokenExpiry expiry = new TokenExpiry(token.getValue(), token.getExpiration());
			// Remove existing expiry for this token if present
			expiryQueue.remove(expiryMap.put(token.getValue(), expiry));
			this.expiryQueue.put(expiry);
		}
		if (token.getRefreshToken() != null && token.getRefreshToken().getValue() != null) {
			this.refreshTokenToAccessTokenCache.put(token.getRefreshToken().getValue(), token.getValue());
			this.accessTokenToRefreshTokenCache.put(token.getValue(), token.getRefreshToken().getValue());
		}
	}

	private String getApprovalKey(OAuth2Authentication authentication) {
		String userName = authentication.getUserAuthentication() == null ? "" : authentication.getUserAuthentication().getName();
		return getApprovalKey(authentication.getOAuth2Request().getClientId(), userName);
	}

	private String getApprovalKey(String clientId, String userName) {
		return clientId + (userName == null ? "" : ":" + userName);
	}

	@SuppressWarnings("unchecked")
	private void addToCollection(Cache cache, String key, OAuth2AccessToken token) {
		ValueWrapper vw = cache.get(key);
		if (vw == null) {
			synchronized (cache) {
				vw = cache.get(key);
				if (vw == null) {
					cache.put(key, new HashSet<OAuth2AccessToken>());
				}
			}
		}
		vw = cache.get(key);
		if (vw != null) {
			((HashSet<OAuth2AccessToken>) vw.get()).add(token);
		}
	}

	public void removeAccessToken(OAuth2AccessToken accessToken) {
		removeAccessToken(accessToken.getValue());
	}

	public OAuth2AccessToken readAccessToken(String tokenValue) {
		return (OAuth2AccessToken) this.accessTokenCache.get(tokenValue).get();
	}

	@SuppressWarnings("unchecked")
	public void removeAccessToken(String tokenValue) {
		ValueWrapper vw = this.accessTokenCache.get(tokenValue);
		OAuth2AccessToken removed = null;
		if (vw != null) {
			removed = (OAuth2AccessToken) vw.get();
		}
		this.accessTokenToRefreshTokenCache.evict(tokenValue);
		// Don't remove the refresh token - it's up to the caller to do that
		OAuth2Authentication authentication = null;
		vw = this.authenticationCache.get(tokenValue);
		if (vw != null) {
			authentication = (OAuth2Authentication) vw.get();
		}
		this.authenticationCache.evict(tokenValue);
		if (authentication != null) {
			this.authenticationToAccessTokenCache.evict(authenticationKeyGenerator.extractKey(authentication));
			Collection<OAuth2AccessToken> tokens;
			tokens = (Collection<OAuth2AccessToken>) this.userNameToAccessTokenCache.get(authentication.getName()).get();
			if (tokens != null) {
				tokens.remove(removed);
			}
			String clientId = authentication.getOAuth2Request().getClientId();
			tokens = null;
			vw = this.clientIdToAccessTokenCache.get(clientId);
			if (vw != null) {
				tokens = (Collection<OAuth2AccessToken>) vw.get();
				if (tokens != null) {
					tokens.remove(removed);
				}
			}
			
			this.authenticationToAccessTokenCache.evict(authenticationKeyGenerator.extractKey(authentication));
		}
	}

	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		this.refreshTokenCache.put(refreshToken.getValue(), refreshToken);
		this.refreshTokenAuthenticationCache.put(refreshToken.getValue(), authentication);
	}

	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		OAuth2RefreshToken token = null;
		ValueWrapper o = this.refreshTokenCache.get(tokenValue);
		if (o != null) {
			token = (OAuth2RefreshToken) o.get();
		}
		return token;
	}

	public void removeRefreshToken(OAuth2RefreshToken refreshToken) {
		removeRefreshToken(refreshToken.getValue());
	}

	public void removeRefreshToken(String tokenValue) {
		this.refreshTokenCache.evict(tokenValue);
		this.refreshTokenAuthenticationCache.evict(tokenValue);
		this.refreshTokenToAccessTokenCache.evict(tokenValue);
	}

	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
		removeAccessTokenUsingRefreshToken(refreshToken.getValue());
	}

	private void removeAccessTokenUsingRefreshToken(String refreshToken) {
		ValueWrapper vw = this.refreshTokenToAccessTokenCache.get(refreshToken);
		if (vw != null) {
			this.refreshTokenToAccessTokenCache.evict(refreshToken);
			String accessToken = (String) vw.get();
			if (accessToken != null) {
				removeAccessToken(accessToken);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
		ValueWrapper vw = userNameToAccessTokenCache.get(getApprovalKey(clientId, userName));
		if (vw == null) {
			return OA_TOKEN_EMPTY_LST;
		}
		Collection<OAuth2AccessToken> result = (Collection<OAuth2AccessToken>) vw.get();
		return result != null ? Collections.<OAuth2AccessToken> unmodifiableCollection(result) : OA_TOKEN_EMPTY_LST;
	}

	@SuppressWarnings("unchecked")
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		ValueWrapper vw = clientIdToAccessTokenCache.get(clientId);
		if (vw == null) {
			return OA_TOKEN_EMPTY_LST;
		}
		Collection<OAuth2AccessToken> result = (Collection<OAuth2AccessToken>) vw.get();
		return result != null ? Collections.<OAuth2AccessToken> unmodifiableCollection(result) : OA_TOKEN_EMPTY_LST;
	}

	private void flush() {
		TokenExpiry expiry = expiryQueue.poll();
		while (expiry != null) {
			removeAccessToken(expiry.getValue());
			expiry = expiryQueue.poll();
		}
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	private static final class TokenExpiry implements Delayed {
		private final long expiry;
		private final String value;

		public TokenExpiry(String value, Date date) {
			this.value = value;
			this.expiry = date.getTime();
		}

		public int compareTo(Delayed other) {
			if (this == other) {
				return 0;
			}
			long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
			return (diff == 0 ? 0 : ((diff < 0) ? -1 : 1));
		}

		public long getDelay(TimeUnit unit) {
			return expiry - System.currentTimeMillis();
		}

		public String getValue() {
			return value;
		}
	}
	
}