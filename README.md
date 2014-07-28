# Crowd crisis movement for the ONE simulator [![Build Status](https://travis-ci.org/davidsan/one.png)](https://travis-ci.org/davidsan/one)


A fork of the Opportunistic Network Environment simulator (the ONE) featuring crisis crowd movement.

Generated Javadoc can be found at : [https://davidsan.github.io/one](https://davidsan.github.io/one) or can be generated using `doc/create_docs.sh` script.

Repository can be found at : [https://github.com/davidsan/one](https://github.com/davidsan/one).

For more information on the ONE, visit [http://www.netlab.tkk.fi/tutkimus/dtn/theone](http://www.netlab.tkk.fi/tutkimus/dtn/theone).


## Implementations
### Crowd movement
Crowd movement is implemented with the `DangerMovement` class and his sub-models (`EvacuationCenterMovement`, `HomeMovement`, `RandomPathMapBasedMovement`, `ShortestPathMapBasedPoiMovement`, `SosMovement`).

### Closed roads
Closed roads are implemented using `AccidentGenerator` which generate `AccidentEvent`. Those events make changes to the `MapNode`'s instances by calling the close() method.


## Changes
This fork is based on version 1.5.1 RC2 of the ONE simulator.

### Core
* `core.DTNHost` : added known positions, known accidents, warned and stucked states... 
* `core.NetworkInterface` : disable interface when node is at evacuation center
* `core.SimScenario` : store the time at which the number of nodes to save has been reached

### Movement
* `movement.DangerMovement` : danger movement model
* `movement.EvacuationCenterMovement` : movement model for nodes at evacuation centers
* `movement.HomeMovement` : movement model for nodes at home
* `movement.RandomPathMapBasedMovement` : movement model for node walking randomly
* `movement.ShortestPathMapBasedPoiMovement` : movement model for node walking to an evacuation center
* `movement.SosMovement` : movement model for stranded nodes
* `movement.map.PointsOfInterestEvac` : POI for evacuation centers
* `movement.map.DijkstraPathFinder` : Dijkstra path finder with accidents discovery
* `movement.map.DijkstraPathFinderOptimal` : same as DijkstraPathFinder but only compute when necessary
* `movement.HotspotMovement` : movement model for hotspot nodes

### Routing and applications
* `routing.DirectDelivery2` : direct delivery router with explicit removal of delivered messsages 
* `applications.DangerApplication` : send alert message with known locations of other hosts and known accidents, and process received messages
* `applications.HotspotApplication` : application for hotspot, only process received messages created from DangerApplication

### Events
* `input.EmptyEvent` : an empty event doing nothing
* `input.AccidentEvent` : an event for closing randomly selected junctions
* `input.AccidentGenerator` : generator of accidents

### GUI
* `movement.map.MapNode` : added new property for closed map node
* `movement.map.SimMap` : added evacuation centers' locations
* `gui.playfield.NodeGraphic` : for coloring the node according to the movement model
* `gui.playfield.MapGraphic` : for coloring the road closed
* `gui.GUIControls` : raised default speed in GUI mode 

### Database and reports
* `db.Database` : handling database connections
* `db.Queries` : reader for queries properties file
* `db/queries.properties` : properties files for SQL request
* `report.ReportDB` : generic class for report into SQL database
* `report.MovementReportDB` : movement report into database
* `report.LocationReportDB` : location report into database
* `report.TimeReportDB` : various time report into database
* `report.ReportCSV` : generic class for report into CSV file
* `report.LocationReportCSV` : location report into CSV file

### Others 
* `tools/computeWorldSize.sh` : compute MovementModel.worldSize given a WKT map file
* `tools/pretty_print_db.sh` : shortcut to display a SQLite database
* `test.AllWorkingTests` : JUnit tests for passing tests as of v1.5.1 RC2 of the ONE simulator
* `.travis.yml` : Travis-CI file
* `build.xml` : Ant file

### Scripts for cluster using OAR batch scheduler
* `bench/build-project.sh` : compile the project in a cluster's node
* `bench/csv2db.sh` : convert CSV file into SQLite database
* `bench/launch-jobs.sh` : submit multiple ONE simulation jobs to the OAR batch scheduler
* `bench/process-csv.sh` : convert multiple CSV files into SQLite databases using cluster's nodes
* `bench/task.sh` : single job submitted by launch-jobs.sh

### Maps
* `santiago.wkt` : surface : unknown, population : unknown
* `HelsinkiMedium/roads.wkt` : surface : 60 km^2, population : 124K, population 16-74 years old using a smartphone : 60K
* `usantiago.wkt` : surface : 2,5 km^2, population : 26K
* `usantiago2.wkt` : surface : 2,5 km^2, population : 26K (cropped roads)
* `usach.wkt` : surface : 0.8 km^2, population : 6K
* `santiago_center.wkt` : surface : 30 km^2, population 437K

Associated POI files can be found with the map name suffixed with `_pois` (eg. `santiago_center_pois.wkt`)

`usantiago2` (1 POI) and `santiago_center` (3 POI) have their POIs located at various hospitals' location. 



## Example of scenario
An example of scenario can be found in the `danger_settings.txt` file.


## Authors
* Virginie Collombon (`virginie.collombon {at} gmail.com`)
* David San (`davidsanfr {at} gmail.com`)
