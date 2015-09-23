# scaleus
A high performance and transactional triple store 

## Run 

Simple run the application at [http://localhost/app/](http://localhost/app/):
```
java -jar target/scaleus-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Run in another port:
```
java -jar target/scaleus-1.0-SNAPSHOT-jar-with-dependencies.jar -p {port}
```

Run and import data:
```
java -jar target/scaleus-1.0-SNAPSHOT-jar-with-dependencies.jar -d {dataset_name} -i {file_location}
```

## REST API

**List all datasets available:**

```
GET /api/v1/dataset/ HTTP/1.1
Content-Type: application/json
```

**Add dataset:**

```
POST /api/v1/dataset/{dataset} HTTP/1.1
```

**Remove dataset:**

```
DELETE /api/v1/dataset/{dataset} HTTP/1.1
```

**SPARQL endpoint:**

```
GET /api/v1/sparqler/{dataset}/sparql?query={query}&inference={false|true}&format={json|rdf|ttl|rdf|text|csv} HTTP/1.1
Content-Type: application/json
```

**Store triple:**

```
POST /api/v1/store/{dataset} HTTP/1.1
Content-Type: application/json

{
	"s":"http://bioinformatics.ua.pt",
	"p":"http://purl.org/dc/elements/1.1/title",
	"o":"University of Aveiro"
}
```

**Remove triple:**

```
DELETE /api/v1/remove/{dataset} HTTP/1.1
Content-Type: application/json

{
	"s":"http://bioinformatics.ua.pt",
	"p":"http://purl.org/dc/elements/1.1/title",
	"o":"University of Aveiro"
}
```

**Add namespace:**

```

POST /api/v1/namespace/{dataset} HTTP/1.1
Content-Type: application/json

{
  "prefix": "coeus",
  "namespace": "http://bioinformatics.ua.pt/coeus/"
}
```

**Remove namespace:**

```

DELETE /api/v1/namespace/{dataset}/coeus HTTP/1.1
Content-Type: application/json

{
  "prefix": "coeus",
  "namespace": "http://bioinformatics.ua.pt/coeus/"
}
```

**Get namespaces:**

```
GET /api/v1/namespaces/{dataset} HTTP/1.1
Content-Type: application/json
```
