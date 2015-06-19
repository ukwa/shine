# WAR support
This is for deploying to application containers such as Tomcat, Jetty, etc. Currently only tested with Tomcat 7.

Due to the way the Play Framework is designed it is only recommended to run one Play App per application container.

## Build the WAR file
Run the command `play war` and it will assemble a war file in the `target` directory.

## Deploying the WAR file
Copy the WAR file to the Tomcat's `webapp` directory and call it `shine.war`.

For example `cp -v target/shine-1.0.0-SNAPSHOT.war ~/apache-tomcat-7.0.62/webapps/shine.war`

Shine will be available on [http://localhost:8080/shine/search](http://localhost:8080/shine/search)

## Runtime configuration
To override the `application.conf` bundled with the WAR file set the following option in the `CATALINA_OPTS` environment variable before starting the Tomcat:

```
export CATALINA_OPTS=-Dconfig.file=/full/path/to/shine.conf
startup.sh
```

### Example minimal configuration
```
application.secret="VALjnUt>B8Q:1c2AwK`HfM2B>7nTlqIyN?F_ftWu1_dgIMtE0bluRTP;xjZ@OKse"
application.langs="en"

db.default.driver=org.postgresql.Driver
db.default.url="jdbc:postgresql://localhost/shine"
db.default.user=shine
db.default.password=p4ssw0rd

ebean.default="models.*"

application.context="/shine"

shine {
    host = "http://192.168.1.181:8983/solr/jisc5",
    http {
    },
    facets {
        basic {
            crawl_years { name="Crawl Years", limit=5, maxLimit=10 },
            public_suffix { name="Public Suffix", limit=5, maxLimit=10 },
            domain { name="Domain", limit=10, maxLimit=10 },
            content_type_norm { name="General Content Type", limit=3, maxLimit=10 }
        },
        additions {
            content_language { name="Language", limit=5, maxLimit=10 },
            postcode_district { name="Postcode District", limit=5, maxLimit=10 }
        },
        links {
            links_domains { name="Links Domains", limit=5, maxLimit=10 },
            links_public_suffixes { name="Links to Public Suffixes", limit=5, maxLimit=10 }
        },
        entities {
        },
        format {
        }
        collection {
        }
    },
    sorts {
        crawl_year="Crawl Year"
    },
    per_page = 10
    default_from_year = 2000
    default_end_year = 2015
    max_number_of_links_on_page = 10
    max_viewable_pages = 50
    facet_limit = 5
    show_browse = false
    show_collections_field = false
    show_concordance = false
    resource_limit = 10
    csv_max_limit = 20000
    csv_interval_limit = 1000
    web_archive_url = "http://web.archive.org/web"
}
```

## Other documentation
- https://github.com/play2war/play2-war-plugin/wiki/Configuration
- https://github.com/play2war/play2-war-plugin/wiki/Deployment
- https://github.com/play2war/play2-war-plugin/wiki/FAQ

