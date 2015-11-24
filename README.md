# SCALEUS
RESTfull services over an easy, high performance and transactional triple store.

[AngularJS](https://angularjs.org/) + [Jetty Embedded](http://www.eclipse.org/jetty/) (RESTfull API) + [Apache Jena Framework](https://jena.apache.org/)

## Main Features:

- Easy to deploy.
- Simple RESTfull API for data management.
- RDFS Inference support over SPARQL queries. Inference rules are also supported.
- Multiple datasets support.

## Run 

Simple run the application at [http://localhost/scaleus/](http://localhost/scaleus/):
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
GET /api/v1/sparqler/{dataset}/sparql?query={query}&inference={inference}&rules={rules}&format={format} HTTP/1.1
Content-Type: application/json

Sample parameters:

query = SELECT * { ?s ?p ?o } LIMIT 100
inference = true | false
rules = [rule1:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)] [rule2: ... ]
format = json | rdf | ttl | rdf | text | csv

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
DELETE /api/v1/store/{dataset} HTTP/1.1
Content-Type: application/json

{
	"s":"http://bioinformatics.ua.pt",
	"p":"http://purl.org/dc/elements/1.1/title",
	"o":"University of Aveiro"
}
```

**Add namespace:**

```

POST /api/v1/namespaces/{dataset} HTTP/1.1
Content-Type: application/json

{
  "prefix": "coeus",
  "namespace": "http://bioinformatics.ua.pt/coeus/"
}
```

**Remove namespace:**

```

DELETE /api/v1/namespaces/{dataset}/{prefix} HTTP/1.1
Content-Type: application/json

```

**Get namespaces:**

```
GET /api/v1/namespaces/{dataset} HTTP/1.1
Content-Type: application/json
```

**Get resources matching:**

```
GET /api/v1/resources/{dataset}/{match} HTTP/1.1
Content-Type: application/json
```

**Get properties matching:**

```
GET /api/v1/properties/{dataset}/{match} HTTP/1.1
Content-Type: application/json
```

**Get TTL data:**

```
GET /api/v1/data/{dataset} HTTP/1.1
Content-Type: text/plain
```

**Replace TTL data:**

```
POST /api/v1/data/{dataset} HTTP/1.1
Content-Type: application/x-www-form-urlencoded

@prefix coeus: <http://bioinformatics.ua.pt/coeus/> .
@prefix dc:    <http://purl.org/dc/elements/1.1/> .

coeus:ieeta dc:title "University of Aveiro" .
```

**Describe Resource:**

```
GET /api/v1/resource/{dataset}/{prefix}/{resource}/{format} HTTP/1.1

Sample parameters:

format = json | rdf | ttl | ld 
```

## Python API

[scaleus-python](https://github.com/bioinformatics-ua/scaleus-python)
