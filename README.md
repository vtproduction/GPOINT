#GamePoint

#API
Base Url: `http://li1064-55.members.linode.com`

####
##Event List
Bearer Token: Needed to make requests: Provided at login response

####Response
```[  
   {  
      "id":1,
      "name":"Test",
      "venue_id":1,
      "sport_id":1,
      "team1":"Bears",
      "team2":"Lions",
      "point_value":1000,
      "minutes_to_redeem":15,
      "start_time":"2019-06-08T16:00:00.000Z",
      "picks_allowed":true,
      "pick_points":200,
      "venue":{  
         "id":1,
         "name":"Test",
         "address":{  
            "id":2,
            "street":"123 New St",
            "city":"Minneapolis",
            "state_id":23,
            "zipcode":null,
            "business_id":null,
            "venue_id":1,
            "latitude":null,
            "longitude":null
         },
         "url":"http://li1064-55.members.linode.com/venues/1.json"
      },
      "sport":{  
         "id":1,
         "name":"Football",
         "icon":"http://li1064-55.members.linode.com//system/sports/icons/000/000/001/original/Football.svg?1559928717"
      },
      "url":"http://li1064-55.members.linode.com/events/1.json"
   },
   {  
      "id":2,
      "name":"Bears vs Lions",
      "venue_id":1,
      "sport_id":1,
      "team1":"Bears",
      "team2":"Lions",
      "point_value":1200,
      "minutes_to_redeem":12,
      "start_time":"2019-06-09T17:00:00.000Z",
      "picks_allowed":false,
      "pick_points":null,
      "venue":{  
         "id":1,
         "name":"Test",
         "address":{  
            "id":2,
            "street":"123 New St",
            "city":"Minneapolis",
            "state_id":23,
            "zipcode":null,
            "business_id":null,
            "venue_id":1,
            "latitude":null,
            "longitude":null
         },
         "url":"http://li1064-55.members.linode.com/venues/1.json"
      },
      "sport":{  
         "id":1,
         "name":"Football",
         "icon":"http://li1064-55.members.linode.com//system/sports/icons/000/000/001/original/Football.svg?1559928717"
      },
      "url":"http://li1064-55.members.linode.com/events/2.json"
   }
]```