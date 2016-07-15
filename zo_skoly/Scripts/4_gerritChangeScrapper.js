(function() {
'use strict';

var options = {
    connect: function (client) {
        var cp = client.connectionParameters;
        //console.log("Connected to database:", cp.database);
    }
};

var pgp = require('pg-promise')(options);
var request = require('request');
var db = pgp('postgres://wv011400:bzodybog@46.229.230.245:5432/wv011401db');
var fs = require("fs");
var invalid = 0;

db.query("SELECT * FROM commits WHERE change_id in (SELECT change_id FROM commits GROUP BY change_id HAVING COUNT(change_id)=1)")
    .then(function(data){
      data.forEach(function(e,i){
          getChangeNumber(e.change_id.trim());
      });
    })
    .catch(function (error) {
        console.error("Selecting from commits failed: "+ error);
    });

function getChangeNumber(changeId) {
    var url = "https://git.eclipse.org/r/changes/?q=foo&n=25&O=81";
    url = url.replace("foo", changeId);

  request(url, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      var jsonString = body.toString().slice(4, body.length-1);
      var jsonObj = JSON.parse(jsonString);
      if(jsonObj[0] != undefined){
        insertGerritChange(jsonObj[0]["_number"], changeId);
      } else {
        console.error("JSON could not be parsed: " + jsonString);
      }
    } else {
      console.error("Error occured while making http request: "+response.statusCode);
    }
  })
}

function insertGerritChange(changeNumber, link) {
  var url = "https://git.eclipse.org/r/changes/foo/detail";
  url = url.replace("foo", changeNumber);

  request(url, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      var jsonString = body.toString().slice(4, body.length-1);
      var jsonObj = JSON.parse(jsonString);

      if(jsonObj != undefined && jsonObj.owner != undefined && jsonObj.status != undefined
        && jsonObj.messages != undefined) {

          var authorName = jsonObj.owner.name;
          var authorId = jsonObj.owner["_account_id"];
          var changeStatus = jsonObj.status;
          var patchCount = 0;

          jsonObj.messages.forEach(function (e,i){
            if(e.message != undefined) {
              if(e.message.indexOf('Build Started') > -1) {
                patchCount++;
              }
            } else {
              patchCount++;
            }
          });
          var staus = changeStatus == "MERGED" ? true : false;

          db.query("INSERT INTO gerrit_change (author, patches_count, merged, link) VALUES ((SELECT id from developers WHERE name=$4), $1, $2, $3)", [patchCount, staus, link, authorName])
              .then(function(data){
              })
              .catch(function (error) {
                  console.error("Inserting into gerrit_change: " + error);
              });

        } else {
            console.error("JSON could not be parsed or does not contain needed attribute: " + jsonString);
        }
    } else {
      console.error("Error occured while making http request: "+response.statusCode);
    }
  })

}

}());
