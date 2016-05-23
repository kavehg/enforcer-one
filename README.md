# Enforcer One

Enforcer One is a simple process monitoring tool.  Start an "X-Wing" process on each host where you would like to monitor processes.  The X-Wings will report their all process changes to the "Death Star".

The Application is broken into 3 Major Components:

  - X-Wing (Process monitor)
  - DeathStar (Web app)
  - DeathStarClient (Client API used by X-Wings to communicate with the Death Star)

## Monitoring Console

Once the Death Star web application has been started you can watch the monitoring console for any changes reported by the X-Wings. Support teams can drag new events to the acknowledged column. If events are not acknowledged, they are automatically moved to the "Escalated" column and escalation is triggered.

![Alt text](/images/screen.png?raw=true "Monitoring Console")

## Running

Use `mvn package` to compile and `java -jar death-star-1.0-SNAPSHOT.jar` to run. Same steps for the X-Wings.

## Dependencies

Enforcer One has the following dependencies

  - [Netty](http://netty.io)
  - [AngularJS](http://angularjs.org)
  - [Materialize CSS](http://materializecss.com)
  - [ngDraggable](https://github.com/fatlinesofcode/ngDraggable)
  - [jQuery](http://jquery.com)
  - [Jersey](https://jersey.java.net)

License
----
Copyright 2015 Kaveh Ghahremani

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
