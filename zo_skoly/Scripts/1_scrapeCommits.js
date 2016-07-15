(function() {
'use strict';
var options = {
    connect: function (client) {
        var cp = client.connectionParameters;
    }
};

var pgp = require('pg-promise')(options);
var Crawler = require("simplecrawler");
var cheerio = require('cheerio');
var fs = require('fs');
var date = new Date();
var start;
var devId, fileId, fileExists, devExists;

var db = pgp('postgres://wv011400:bzodybog@46.229.230.245:5432/wv011401db');

var crawler = new Crawler()
crawler.interval = 250;
crawler.maxDepth = 1;
crawler.maxConcurrency = 7;

//Get commit links from file
var dataCommitsUrl = fs.readFileSync('commits_urls.txt');
var array = dataCommitsUrl.toString().split('\n');
array.splice(array.length-1, 1);
array.forEach(function(element, index) {
  crawler.queue.add('http', 'git.eclipse.org', 80, element);
});


crawler.on('complete', function(){
  console.log("Crawl complete.");
  console.log("Running time: (s)"+(Date.now() - start)/1000);
});

crawler.on("crawlstart", function() {
  console.log("Crawl starting.");
  start = Date.now();
});

crawler.on("fetchstart", function(queueItem) {

});

crawler.on("fetchcomplete", function(queueItem, responseBuffer, response) {

  var $ = cheerio.load(responseBuffer);
  //Get dev's name
  var devName = $('.commit-info').children('tr:nth-child(1)')
    .children('td:nth-child(2)').text().trim();

  //Add dev to db
  db.any("select * from developers WHERE name LIKE '%$1^%'", devName)
      .then(function (data) {
          if(data.length > 0) {
            devId = data[0].id;
            handleFiles(responseBuffer, devId);
          } else {
            db.one("INSERT INTO developers(name) VALUES($1) returning id", devName)
                .then(function (data) {
                    devId = data.id;
                    handleFiles(responseBuffer, devId);
                })
                .catch(function (error) {
                    console.error("Inserting into developers failed: "+ error);
                });
          }
      })
      .catch(function (error) {
          console.error("Selecting from developers failed: "+ error);
      });
});

crawler.on("fetcherror", function(queueItem, response) {
  console.error("An error occured while fetching " + queueItem + " with respose"
  + respose);
});

//Add relation to mapping table
function insertDevFiles(commitId, fileId, changeCount) {
  var ids = {
    commitId: commitId,
    fileId: fileId,
    changeCount: changeCount
  }

  db.one("INSERT INTO files_commits(commit_id, file_id, change_count) VALUES(${commitId}, ${fileId},  ${changeCount}) returning id", ids)
      .then(function (data) {

      })
      .catch(function (error) {
          console.error("Inserting into files_commits failed: "+ error);
      });
}

// Get changed files and add them to db
function handleFiles(responseBuffer, devId) {
  var $ = cheerio.load(responseBuffer);
  var changeId;
  var commitInforText = $(".commit-msg").text();
  var bugUrl;

  if(commitInforText.indexOf("Task-Url") > -1) {
    var lines = commitInforText.split('\n');
    for(var i = 0; i < lines.length; i++) {
      if(lines[i].indexOf("Task-Url") > -1){
        var arr = lines[i].split(" ");
        bugUrl = arr[1].trim();
      }
    }
  }
  if(commitInforText.indexOf("Change-Id") > -1) {
    var lines = commitInforText.split('\n');
    for(var i = 0; i < lines.length; i++) {
      if(lines[i].indexOf("Change-Id") > -1){
        var arr = lines[i].split(":");
        changeId = arr[1].trim();
      }
    }
  }

  // insert into commit
  var ids = {
    devId: devId,
    bugUrl: bugUrl,
    changeId: changeId
  }

  db.one("INSERT INTO commits(developer_id, bug_id, change_id) VALUES(${devId}, ${bugUrl}, ${changeId}) returning id", ids)
      .then(function (data) {
        var commitId = data.id;
        handleIndividualFiles($, commitId);
      })
      .catch(function (error) {
          console.error("Inserting into commits failed: "+ error);
      });
}

function handleIndividualFiles($, commitId) {
  $('.upd a').each(function(i,e) {

    var name = $(e).text().trim();
    var changeCount = $(e).parent().parent().children('td:nth-child(3)').text().trim();

    db.any("select * from files WHERE name LIKE '%$1^%'", name)
        .then(function (data) {
            if(data.length > 0) {
              var fileId = data[0].id;
              insertDevFiles(commitId, fileId, changeCount);
            } else {
              db.one("INSERT INTO FILES(name) VALUES($1) returning id", name)
                  .then(function (data) {
                      var fileId = data.id;
                      insertDevFiles(commitId, fileId, changeCount);
                  })
                  .catch(function (error) {
                      console.error("Inserting into files failed: "+ error);
                  });
            }
        })
        .catch(function (error) {
            console.error("Selecting from files failed: "+ error);
        });
  });
}


crawler.start();
}());
