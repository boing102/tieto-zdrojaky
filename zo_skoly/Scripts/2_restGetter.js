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
var fs = require("fs");
var db = pgp('postgres://wv011400:bzodybog@46.229.230.245:5432/wv011401db');

//getChangeNumber("Idac6aec404f7dfaeec89935e4f13cc0b56990940",1);

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
        changeDeveloperNames(jsonObj[0]["_number"]);
      } else {
        console.error("JSON could not be parsed: "+jsonObj);
      }
    } else {
      console.error("Error occured while making http request: "+response.statusCode);
    }
  })
}

function changeDeveloperNames(changeNumber) {
  var url = "https://git.eclipse.org/r/changes/foo/detail";
  url = url.replace("foo", changeNumber);

  request(url, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      var jsonString = body.toString().slice(4, body.length-1);
      var jsonObj = JSON.parse(jsonString);
      if(jsonObj != undefined && jsonObj.labels != undefined
        && jsonObj.labels['Code-Review'] && jsonObj.labels['Code-Review'].all) {

          jsonObj.labels['Code-Review'].all.forEach(function(e,i){
            db.any("select * from developers where name=$1", e.name)
                .then(function (data) {

                  if(data.length > 0) {
                    //update code reviewer count
                    if(data[0]["cr_count"] == null) {
                      data[0]["cr_count"] = parseInt(1, 10);
                    } else {
                      data[0]["cr_count"] = parseInt(data[0]["cr_count"],10) + 1;
                    }

                    //save to db ${commitId}, ${fileId}
                    db.any("update developers set cr_count = ${cr_count} where id = ${id}", data[0])
                        .then(function (data) {

                        })
                        .catch(function (error) {
                            console.error("Updating developers failed: "+ error);
                        });
                  }
                })
                .catch(function (error) {
                    console.log("Selecting from developers failed: "+ error);
                });
          });
        } else {
          console.log("JSON could not be parsed: "+ jsonString);
        }
    } else {
      console.error("Error occured while making http request: "+response.statusCode);
    }
  })

}


}());
