# lazyloader
Java generic lazy loader. Create a LazyLoader for iterating data. In the background, it dynamically read data from a data source in batches. This could be useful when you need to read and process data from a DB sequentially, aiming to locate a particular row but the row cannot be identified by WHERE clause (eg. if the data is encrypted).
