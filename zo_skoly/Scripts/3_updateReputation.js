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
var maxCodeReviewer;

  db.any("select * from developers where cr_count is not null order by cr_count desc")
    .then(function (data){
      if(data.length > 0) {
        maxCodeReviewer = data[0]["cr_count"];
        computeReputation();
      }
    }).catch(function (error) {
      console.error("Selecting from developers failed: "+ error);
    });

  function computeReputation() {
    db.any("select * from developers ")
      .then(function (data) {
        if(data.length > 0) {
          data.forEach(function(e,i) {
            if(e["cr_count"] != null) {
              e["reputation"] = parseFloat(e["cr_count"]) / maxCodeReviewer;
              e["reputation"] = Math.round(e["reputation"] * 1000) / 1000;

              db.any("update developers set reputation = ${reputation} where id = ${id}", e)
              .then(function (data) {}).catch(function (error) {
                console.error("Updating developers failed: "+ error);
              });
            }
          });
        }
        }).catch(function (error) {
          console.error("Selecting from developers failed: "+ error);
        });
  }
}());
