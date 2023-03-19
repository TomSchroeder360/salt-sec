

TODO - 
** when saving payloads we might want to find a way to reduce dynamic variables in the url, 
since this might quickly blow up our cache.

** Need to add redis cache, probably using nest.js.
1. load our caches on startup from the redis server.
2. set an exceptable ttl on local cache, incase models change.
3. fallback to redis if entity not found locally.
4. cache the response even if response is empty.

** VerifierRoute -> save. Need to handle the NoSuchElementException case
1. the case should be removed.
2. the cause for these failures (None.get) should be handled accordingly.
    1. use InvalidMessageBodyFailure with relevant message.
    2. can add an explicit failure message on the toTry function. 
These are the majority of the failures when receiving incorrect payloads.
