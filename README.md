# LazyLoader

**LazyLoader** is a Java-based generic utility designed to facilitate the lazy loading of data. It allows for the efficient iteration over datasets by dynamically fetching data in batches from a specified data source. This approach is particularly beneficial in scenarios where data needs to be sequentially read and processed from a database, especially when direct identification of rows using a `WHERE` clause is not feasible (e.g., when dealing with encrypted data).

## Features

- **Generic Implementation**: Can be used with any data type, providing flexibility across various use cases.
- **Batch Processing**: Dynamically loads data in batches, optimizing memory usage and processing time.
- **Sequential Data Access**: Ideal for scenarios where data must be processed in sequence without direct row identification.
- **Encrypted Data Handling**: Facilitates the processing of encrypted data that cannot be directly queried with specific conditions.

## Use Case

The **LazyLoader** is particularly useful in applications where large datasets are involved, and only a portion of the data is needed at any given time. It's also beneficial in situations where the dataset is too large to be loaded into memory all at once or when dealing with encrypted data that requires sequential access to locate specific information.

## How It Works

1. **Initialization**: A `LazyLoader` instance is created with parameters defining the data source, batch size, and other relevant settings.
2. **Iterating**: As the user iterates over the `LazyLoader`, data is fetched in batches from the data source as needed.
3. **Dynamic Loading**: When the end of the currently loaded data is reached, the next batch is automatically fetched, ensuring a seamless iteration experience.

## Getting Started

To use **LazyLoader** in your project, simply instantiate it with the appropriate parameters for your data source and batch size. Then, use it like any other iterable collection in Java.

```java
LazyLoader<Model, Context> lazyLoader = new LazyLoader<>(context, dao, batchSize, meterManager, ...);
for (Model model : lazyLoader) {
    // Process each model
}
