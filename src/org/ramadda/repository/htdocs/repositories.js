
function NasaRepository() {
    var baseUrl =  "http://data.nasa.gov/api/get_search_results";
    var name= "data.nasa.gov";
    RamaddaUtil.inherit(this,  new Repository(name));
    RamaddaUtil.defineMembers(this, {
            getName: function() {
                return name;
            },
            getIconUrl: function(entry) {
                return "http://data.nasa.gov/favicon.ico";
            },
           getSearchUrl: function(settings, output) {
                var url =  baseUrl;
                var searchText = "data";
                if(settings.text!=null&& settings.text.length>0)  {
                    searchText = settings.text;
                }
                url += "?search=" + encodeURIComponent(searchText);
                console.log(url);
                url =  GuiUtils.getProxyUrl(url);
                return url;
            },
            createEntriesFromJson: function(data) {
                var entries = new Array();
                if(data.posts==null) {
                    console.log("No 'posts' results");
                    return entries;
                }

                for(var i=0;i<data.posts.length;i++) {
                    var post = data.posts[i];
                    var props = {
                        repositoryId: this.getId(),
                        id:"nasa-result-" + i,
                        name: post.title,
                        url: post.url,
                        description:post.excerpt,
                        type:"nasa-" + post.type
                    };
                    entries.push(new Entry(props));
                }
                return entries;
            }
        });

}


function DuckDuckGoRepository() {
    var baseUrl = "http://api.duckduckgo.com/?format=json";
    var name = "DuckDuckGo";
    RamaddaUtil.inherit(this,  new Repository(name));
    RamaddaUtil.defineMembers(this, {
            getName: function() {
                return name;
            },
            getIconUrl: function(entry) {
                return "https://duckduckgo.com/favicon.ico";
            },
           getSearchUrl: function(settings, output) {
                var url =  baseUrl;
                var searchText = "data";
                if(settings.text!=null&& settings.text.length>0)  {
                    searchText =  settings.text;
                }
                url += "&q=" + encodeURIComponent(searchText);
                console.log(url);
                return   GuiUtils.getProxyUrl(url);
            },
            createEntriesFromJson: function(data) {
                var entries = new Array();
                if(!data.RelatedTopics) {
                    console.log("no related topics");
                    return  entries;
                }
                for(var i=0;i<data.RelatedTopics.length;i++) {
                    var topic  = data.RelatedTopics[i];
                    var props = {
                        repositoryId: this.getId(),
                        id:"duckduckgo-result-" + i,
                        name: topic.Text,
                        url: topic.FirstURL,
                        description:topic.Result,
                        type:"duckduckgo-link"
                    };
                    entries.push(new Entry(props));
                }
                return entries;
            }
        });

}





function GoogleRepository() {
    var baseUrl = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0";
    var name = "Google";
    RamaddaUtil.inherit(this,  new Repository(name));
    RamaddaUtil.defineMembers(this, {
            getName: function() {
                return name;
            },
            getIconUrl: function(entry) {
                return "http://www.google.com/favicon.ico";
            },
           getSearchUrl: function(settings, output) {
                var url =  baseUrl;
                var searchText = "data";
                if(settings.text!=null&& settings.text.length>0)  {
                    searchText =  settings.text;
                }
                url += "&q=" + encodeURIComponent(searchText);
                console.log("URL" + url);
                return   GuiUtils.getProxyUrl(url);
            },
            createEntriesFromJson: function(data) {
                var entries = new Array();
                if(!data.responseData) {
                    console.log("no responseData");
                    return  entries;
                }
                for(var i=0;i<data.responseData.results.length;i++) {
                    var result  = data.responseData.results[i];
                    var props = {
                        repositoryId: this.getId(),
                        id:this.getId() +"-result-" + i,
                        type:this.getId() +"-link",
                        name: result.title,
                        url: result.unescapedUrl,
                        description:result.content,
                    };
                    entries.push(new Entry(props));
                }
                return entries;
            }
        });

}





