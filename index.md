## Welcome to OptranUtils

This repository contains utilities that can be used to make programming in java for quick POC/analytical tasks easier. The following are some of the features present in this library.

### Index
- Usage
- DataSet
- PagedFile
- PagedRandomAccessFile

### Usage

Add the following repository to your maven pom file:
``` xml
<repositories>
	<repository>
		<id>Optran</id>
		<url>https://raw.githubusercontent.com/Optran/OptranUtils/repository</url>
	</repository>
</repositories>
```

And add the following dependency

``` xml
<dependency>
	<groupId>com.github.optran.utils</groupId>
	<artifactId>OptranUtils</artifactId>
	<version>21.1.26-ALPHA</version>
</dependency>
```
### DataSet
Often when we are working with data, we need to store data in such a way that can be accessed again and again. Across different analysis tasks this data must not change, even though more data may be added to it. It is also necessary to be able to navigate this data in any direction, forward or reverse. This functionality is provided by the DataSetIO class. This class is created using the DataSetIOBuilder class and can create multiple consumers and producers for a given DataSet. These consumers & producers can then be used to read and write data to and from the DataSet. Consumers and Producers may each be passed to different threads and will work without causing any issues (For now consumers do not have the feature to wait for data to become available once a producer has written something to the DataSet but this will be added in the future). Example usage of this class is as follows:-

``` Java
File file = new File("DataSet.ds");
DataSetIO dataSetIO = new DataSetIOBuilder()
			.withFile(file)			//If this parameter is not provided a temporary file will be created in the users temp directory.
			.withCacheSize(100)		//Sets the number of pages to cache in memory, if this is not provided caching will be disabled.
			.withPageSize(100)		//If a value less than 10 is provided here or this is not set, a default page size of 65536 is used.
			.build();
DataSetConsumer consumer = dataSetIO.createConsumer();
DataSetProducer producer = dataSetIO.createProducer();
producer.writeString("Hello");
producer.write("World".getBytes(StandardCharsets.UTF_8));
while(consumer.next()) {
	System.out.println(consumer.readString());	//read will read a byte[] and readString will convert to string before returning
}
dataSetIO.closeProducer(producer);			//Commits any records added to the DataSet by any producer, not just this one.
							//producer.commit(); achieves the same effect as of now.
dataSetIO.closeConsumer(consumer);
```

### PagedFile
When dealing with a persistent data, and when it is required that the file may be processed out of sequence, we need something like the RandomAccessFile that allows us to jump around inside the file. To simplify data location and access further, it is better to think of a file as a series of "Blocks" or "Pages" of a fixed length.
The PagedFile interface offers exactly this. There are two implimentations of this interface supplied in this library namely.
- **RandomAccessPagedFile:** This implementation makes changes to the file directly on the disk so that any changes made by you are persisted immidiately. (close() still needs to be called to ensure all changes were persisted.). Example usage is as follows.
``` Java
File input = new File("temp.txt");
RandomAccessPagedFile pagedFile = new RandomAccessPagedFile(input, 10);
Page page = pagedFile.readPage(0);
page.write("data".getBytes(StandardCharsets.UTF_8));
page.setHead(0);
byte[]data = new byte[4];
page.read(data);
System.out.println(new String(data, StandardCharsets.UTF_8));
pagedFile.close();
```

- **LRUCachePagedFile:** This implementation uses the RandomAccessPagedFile, but adds a caching layer where the user can specify the number of pages to cache. It is necessary then when persisting the changes to invoke the close() method to persist the changes, and if the system has some idle time flush() can be invoked to persist any pages that have not yet been written.
``` Java
File input = new File("temp.txt");
LRUCachedPagedFile pagedFile = new LRUCachedPagedFile(new RandomAccessPagedFile(input, 10), 10);
Page page = pagedFile.readPage(0);
page.write("data".getBytes(StandardCharsets.UTF_8));
page.setHead(0);
byte[]data = new byte[4];
page.read(data);
System.out.println(new String(data, StandardCharsets.UTF_8));
pagedFile.close();
```

### PagedRandomAccessFile
If one happens to be  dealing with random access files but also wants to leverage caching for better performance, this is the class that allows that. Internally it makes use of the PagedFile to help reduce the amount of Disk IO required to perform a task.
