# MSAssignment - Simple File Server REST API

This project implements a simple file server, allowing an authorised user to upload, download, delete and retrieve a
list of uploaded files. Each operation is exposed through a RESTful interface. The filesystem is handled via an
isolated storage directory.

This application is built using Spring Boot 3.5.7, Spring MVC 6.2.12 and Spring Security with embedded Tomcat 10.1.48.

## Requirements

* Java version 17 or above.
* Apache Maven version 3.9 or later.
* By default, the application runs on port 8080 and this is configurable via the `application.yml` file. The specified
port must be available.

## Installation and running the application

### Default method

The simplest way to run the application is to navigate to the root directory of the project and run it via Maven.

```
mvn spring-boot:run
```

### Direct JAR invokation

It is also possible to build the application and run the resulting JAR directly. This can be run from the generated
`target/` directory. However, the default storage filesystem will also be erased in this scenario. This can be
circumvented by setting the `storage.location` property in the `application.yml` file or by copying the JAR to a
dedicated filesystem or directory.

```
mvn clean install
cd target
java -jar MSAssignment-0.0.1-SNAPSHOT.jar
```

### Running using Docker

The application can also be run from Docker. In order to do so, the following command can be run from the root directory
of the project, creating an image for the application named `msassignment`.

```
docker build -t msassignment .
```

This image can then be run through the following command.

```
docker run --name msassignment -p 8080:8080 msassignment:latest
```

## API overview and authentication

The following endpoints are provided by the application. Each of these require authentication via a recognised username
and password. The endpoints are provided relative to the domain. For example, by default and when running using the run
steps above, the Delete Controller will be accessed via `http://localhost:8080/file/delete/{filepath}`.

| Controller | Method | Endpoint                    | Description                                                                            |
|------------|--------|-----------------------------|----------------------------------------------------------------------------------------|
| Delete     | DELETE | `/file/delete/{filepath}`   | Delete a file whose filename is located at `{filepath}` within the storage filesystem. |
| Download   | GET    | `/file/download/{filepath}` | Download the file located at `{filepath}` if it exists.                                |
| List       | GET    | `/file/list`                | List all files in the storage filesystem.                                              |
| Upload     | POST   | `/file/upload/{filepath}`   | Upload the multipart/form-data file to the location specified by `{filepath}`.         |

The API overview can also be consulted using Swagger UI `/swagger-ui.html`. Subdirectories are supported in each of the
controllers listed above (for example, it is possible to upload a file to `filepath = subdir/filename`) when called using
a dedicated RESTful client (e.g., curl, Postman). This support is not possible using Swagger - only files in the root
directory can be deleted, downloaded or uploaded through the web UI.

All controllers and the API documentation require authentication via a recognised username and password. At present, a
single user is stored in memory for this purpose.

* Username = `TEST_USER`.
* Password = `ChangeMe`.

A future consideration would be to store users in a database. Authentication would call a service that queries the user
against the hash-encrypted password that is provided to check that they match. The user would also be assigned a set of
roles describing which of the above endpoints they have access to. For example, a user with Download and List roles
would be able to download and list files but not delete or upload them.

### File Delete Controller

A file can be deleted by supplying its location relative to the root of the storage filesystem. An example is provided
where the file `subdir/file.txt` is deleted from the system using curl.

```
curl -v -u TEST_USER:ChangeMe "http://localhost:8080/file/delete/subdir/file.txt"
```

The following HTTP status codes are returned by this endpoint:

* HTTP 204 if the file was successfully deleted.
* HTTP 400 if the request contains an invalid or disallowed file path.
* HTTP 404 if the file does not exist at the specified location in the filesystem.
* HTTP 500 if an internal server error has occurred.

### File Download Controller

A file can be downloaded by supplying its location relative to the root of the storage filesystem. An example is
provided where the file `subdir/file.txt` is downloaded from the system using curl.

```
curl -v -u TEST_USER:ChangeMe "http://localhost:8080/file/download/subdir/file.txt"
```

The following HTTP status codes are returned by this endpoint:

* HTTP 200 with the file contained in the body of the response if the download was successful.
* HTTP 400 if the request contains an invalid or disallowed file path.
* HTTP 404 if the file does not exist at the specified location in the filesystem.
* HTTP 500 if an internal server error has occurred.

### File Listing Controller

The complete filesystem is listed via the `/file/list` endpoint. An example is provided where this is performed using
curl.

```
curl -v -u TEST_USER:ChangeMe "http://localhost:8080/file/list"
```

The following HTTP status codes are returned by this endpoint:

* HTTP 200 with a JSON array of file paths, relative to the storage root, in the body.
* HTTP 500 if an internal server error has occurred.

### File Upload Controller

A file is uploaded by providing the file as multipart/form-data along with the location to save it to in the storage
filesystem. An example is provided where the file stored locally at `{location}` with file mime-type `{type}` is uploaded to
`{filepath}` using curl.

```
curl -v -u TEST_USER:ChangeMe -F "file=@{location};type={type}" "http://localhost:8080/file/upload/{filepath}"
```

The following HTTP status codes are returned by this endpoint:

* HTTP 201 if the file was uploaded. The `location` parameter in the response header contains the path to download the
file.
* HTTP 409 if a file already exists at the location specified by `{filepath}`. It is not possible to overwrite a file at
present and, although it could be possible to implement this through a query parameter, this is not done here. To
replace a file at the same location, the original must be deleted first.
* HTTP 500 if an internal server error has occurred.

## The storage filesystem

At present, all files are stored in a directory that is local to the server. Its location is defined using the
`storage.location` property in the `application.yml` file. By default, this is set to `storage`. Thus, by default, when
run from the project root (e.g., using `mvn spring-boot:run`), all files will be stored in the `<project root>/storage`
directory.

Unit and end-to-end tests are provided to ensure that `{filepath}` always refers to a location in this filesystem (i.e.,
within this directory or one of its subdirectories). The test suite verifies that path-traversal using `../` or `/` is
not possible (the former is automatically rejected by the embedded Tomcat before reaching any endpoints).

A limitation of this approach is that it is not secure or scalable. At present, files are queried by their relative
filenames, however an ID-based approach would be preferred. This is achievable using appropriate renaming of the files,
however a database-driven filesystem would allow for this. For example, a relational database could hold a table of
files, including their generated ID; a BLOB to the file data itself; other important information such as its mime-type.
This could be joined to another table that indicated which groups and roles had access to each file. This approach would
prevent the need for operating system-specific commands such as `chmod`. At present, as a result, every authenticated
user is able to access (delete, download, list and upload) every file.

## Resilience testing

End-to-end tests are written to test the resilience of the system. In particular, the following failure and race
conditions are handled through the test suite.

* It is not possible to load two files to the same location. An HTTP 409 (conflict) is returned. A race condition is
tested where two users attempt to do this simultaneously. Only the first file that was uploaded will be stored.
* A race condition is also tested where deleting the same file is attempted concurrently. The file should be removed
from the system, but both endpoint calls should return without error. Depending on timing, one call will typically
return HTTP 204 (no content) while the other may return either 204 or 404, but the invariant is that the file is deleted
exactly once.
* It is not possible to download a file that has already been deleted.
* If a file is deleted, it is possible to upload a new file to the same location. This can be performed in lieu of
overwriting the existing file directly.
* The list endpoint should always return a result (except when an internal error has occurred). It should either return
a JSON array of file paths or an empty list. It should not fail if there are no files in the system.
* In particular, if the root storage directory does not exist when an endpoint is called, it should be created.
* It is not possible to call any endpoint with an incorrect username or password.
* It is also not possible to call any endpoint with no credentials supplied at all.
* Path-traversal is not possible. Any query where `{filepath}` contains `..` will be automatically rejected by the
embedded Tomcat. A `{filepath}` that begins with a forward-slash (e.g., `http://localhost:8080/file/download//file.txt`)
will be treated as if the additional `/` was not present.
* Absolute path names can not be used in the `{filepath}`. The physical location will always be determined relative to
the storage root (i.e., the absolute path will be set to `<path to storage root> + filepath`).
* Use of encoded characters is not allowed. For example, using `%20` in place of spaces or `%2F` in place of forward
slashes. Spaces in file paths will be treated as if they form part of the file or directory name.
* The maximum file and request sizes are defaulted to 50MB in the `application.yml` file. A scenario is also tested
where a file is uploaded with an oversized payload. This results in an HTTP 413.

## Limitations and further considerations

The known limitations and suggested improvements are summarised below. Some of these are also discussed in the sections
above but are added here for quick reference. These would be implemented given sufficient time in the project.

* Authentication is of a single user stored in memory using a static username and password. This is not only very
limited, but is also highly insecure.
* A database would be a more appropriate location to store the details of authenticated users. As well as storing their
login credentials, additional tables could be created to describe their access roles.
* Login should take place using a dedicated service, which would call out to the database. If successful, the service
should also return an access token, which the user would provide on successive requests. The access token would be
checked against the stored credential and an HTTP 401 returned if the tokens did not match or the time since the
previous query had passed a pre-set limit.
* Files are stored within a regular directory tree on the same filesystem as the server. This is neither secure nor
scalable. The files should be stored in a dedicated database table.
* Storing the files in a database (even including the `filepath` if desired) would allow IDs to be assigned to each
record. It would then be possible to query each endpoint using the ID of the file.
* Using a database would allow us to store the user who uploaded the file, as well as which users and groups had access
to it. Querying a file that a user did not have access to would produce an HTTP 403. Listing files in the filesystem
would exclude files the user did not have access to. File attributes could also be used: for example, a file could have
read/write access to a user or specific group but read only or no access to other users.
* Additional endpoints could be provided to list files with a specific set of IDs, or files within a subdirectory of a
particular root.
* There is no virus or mime-type checking in the application. A compromised file can be uploaded or downloaded without
error at present.

---

©2025 Nairn McWilliams