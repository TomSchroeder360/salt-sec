

** when saving payloads we might want to find a way to reduce dynamic variables in the url, 
since this might quickly blow up our cache.

** Need to add redis cache, probably using nest.js.
1. load our caches on startup from the redis server.
2. set an exceptable ttl on local cache, incase models change.
3. fallback to redis if entity not found locally.
4. cache the response even if response is empty.

** Some todo's left in the code.
